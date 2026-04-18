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
        const val TAG = "SyncSessionWorker"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "WorkManager woke up! Starting background sync...")

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
                Log.i(TAG, "Attempting to upload ${pendingSessions.size} sessions...")
                val response = api.uploadOfflineSessions(toSessionSyncDtoReq(pendingSessions))

                if (response.isSuccessful) {
                    // 5. WIPE
                    pendingSessionDao.clearAllPendingSessions()
                    Log.i(TAG, "Sync complete! Wiped local cache.")

                    Result.success()
                }
                else {
                    Log.e(TAG, "Server rejected the sync. Code: ${response.code()}")
                    Result.retry()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Network failure during sync: ${e.message}")
                Result.retry()
            }
        }
    }
}