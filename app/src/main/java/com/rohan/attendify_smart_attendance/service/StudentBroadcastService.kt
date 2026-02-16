package com.rohan.attendify_smart_attendance.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.rohan.attendify_smart_attendance.data.ble.BleBroadcastClient
import com.rohan.attendify_smart_attendance.utils.NotificationHelper
import kotlinx.coroutines.*

class StudentBroadcastService : Service() {

    // Scope for background tasks (tied to Service lifecycle)
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    // Assuming 'BleBroadcastClient' is your advertiser class wrapper
    private lateinit var bleAdvertiser: BleBroadcastClient

    // --- CONFIGURATION ---
    private val BROADCAST_WINDOW = 3 * 60 * 1000L // 3 Minutes Broadcasting
    private val REST_WINDOW = 2 * 60 * 1000L      // 2 Minutes Resting
    private val MAX_CLASS_DURATION = 90 * 60 * 1000L // 90 Minutes (Safety Cut-off)

    companion object {
        const val ACTION_START_BROADCAST = "ACTION_START"
        const val ACTION_STOP_BROADCAST = "ACTION_STOP"
        const val EXTRA_STUDENT_ID = "STUDENT_ID"

        // Use the SAME ID as the helper to update the existing notification
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        bleAdvertiser = BleBroadcastClient(this)
        Log.d("StudentService", "✅ Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        // Handle STOP Action
        if (action == ACTION_STOP_BROADCAST) {
            stopSelf()
            return START_NOT_STICKY
        }

        val studentId = intent?.getStringExtra(EXTRA_STUDENT_ID)

        // Validate Input
        if (studentId.isNullOrEmpty()) {
            Log.e("StudentService", "❌ Error: No Student ID provided.")
            stopSelf()
            return START_NOT_STICKY
        }

        // 3. Start Foreground immediately (Crucial for Android 14+)
        startForegroundServicePromotion(isBroadcasting = true)

        // 4. Begin the Cycle
        startBroadcastCycle(studentId)

        // START_STICKY: If OS kills app, recreate it automatically
        return START_STICKY
    }

    private fun startBroadcastCycle(studentId: String) {
        // Reset any previous jobs
        serviceScope.coroutineContext.cancelChildren()

        serviceScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d("StudentService", "🚀 Starting Cycle for: $studentId")

            while (isActive) {
                // SAFETY: Stop after 90 mins
                if (System.currentTimeMillis() - startTime > MAX_CLASS_DURATION) {
                    Log.w("StudentService", "⏰ Max duration reached (90m). Stopping.")
                    stopSelf()
                    break
                }

                // BROADCAST (3 Mins)

                val success = bleAdvertiser.startAdvertising(studentId)
                if (!success) {
                    Log.e("StudentService", "❌ Bluetooth failed. Retrying in 10s...")
                    delay(10_000L)
                    continue
                }
                delay(BROADCAST_WINDOW)

                //REST (2 Mins)
                Log.d("StudentService", "💤 Cycle: RESTING")

                bleAdvertiser.stopAdvertising()
                delay(REST_WINDOW)
            }
        }
    }

    // --- HELPER: Android 14 Compatible Start ---
    private fun startForegroundServicePromotion(isBroadcasting: Boolean) {
        // 1. Get the Notification from your Helper
        val notification = NotificationHelper.createAttendanceNotification(
            context = this,
            isTeacher = false,
            statusText = "Marking Your attendance"
        )

        // 2. Start Foreground with Type Safety
        try {
            if (Build.VERSION.SDK_INT >= 34) { // Android 14 (Upside Down Cake)
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("StudentService", "Foreground Error: ${e.message}")
            stopSelf()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("StudentService", "🛑 Service Destroyed")
        bleAdvertiser.stopAdvertising()
        serviceScope.cancel() // Cancel all coroutines
    }

    override fun onBind(intent: Intent?): IBinder? = null
}