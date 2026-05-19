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
                if (runAttemptCount >= MAX_RETRIES) {
                    Log.e(TAG, "Max retries reached. Server might be down. Stopping sync.")
                    return@withContext Result.failure()
                }
                val app = applicationContext as AttendifyApplication
                val pendingSessionDao = app.database.pendingSessionDao()
                val api = app.api

                val pendingSessions = pendingSessionDao.getAllPendingSessions()

                if (pendingSessions.isEmpty()) {
                    Log.i(TAG, "No pending sessions found. Nothing to sync.")
                    return@withContext Result.success()
                }
                val response = api.uploadOfflineSessions(toSessionSyncDtoReq(pendingSessions))

                if (response.isSuccessful) {
                    pendingSessionDao.clearAllPendingSessions()
                    Result.success()
                } else {
                    val code = response.code()
                    if (code in 400..499) {
                        Log.e(TAG, "Client error $code. Data rejected. Do not retry.")
                        Result.failure()
                    } else {
                        // Server Error .Try again later.
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