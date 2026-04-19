package com.rohan.attendify_smart_attendance.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.data.local.converter.toSessionSyncDtoReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncSessionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "worker"
        const val MAX_RETRIES = 3
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 2. The Battery Saver: Give up after a few tries
                if (runAttemptCount >= MAX_RETRIES) {
                    Log.e(TAG, "Max retries reached. Server might be down. Stopping sync.")
                    return@withContext Result.failure()
                }

                Log.i(TAG, "WorkManager woke up! Attempt $runAttemptCount for background sync...")

                val app = applicationContext as AttendifyApplication
                val pendingSessionDao = app.database.pendingSessionDao()
                val api = app.api

                // 3. READ
                val pendingSessions = pendingSessionDao.getAllPendingSessions()

                if (pendingSessions.isEmpty()) {
                    Log.i(TAG, "No pending sessions found. Nothing to sync.")
                    return@withContext Result.success()
                }

                // 4. SYNC
                Log.i(TAG, "Uploading ${pendingSessions.size} sessions...")
                val response = api.uploadOfflineSessions(toSessionSyncDtoReq(pendingSessions))

                // 5. SMART ROUTING
                if (response.isSuccessful) {
                    // WIPE: The server successfully got the data (and skipped duplicates safely!)
                    pendingSessionDao.clearAllPendingSessions()
                    Log.i(TAG, "Sync complete! Wiped local cache.")
                    Result.success()
                } else {
                    // ERROR HANDLING
                    val code = response.code()
                    if (code in 400..499) {
                        // Client Error (Bad token, bad JSON, etc). Retrying won't fix this.
                        Log.e(TAG, "Client error $code. Data rejected. Do not retry.")
                        Result.failure()
                    } else {
                        // Server Error (5xx). The server is struggling. Try again later.
                        Log.e(TAG, "Server error $code. Retrying later.")
                        Result.retry()
                    }
                }

            } catch (e: Exception) {
                // Network dropped completely
                Log.e(TAG, "Network failure during sync: ${e.message}")
                Result.retry()
            }
        }
    }
}