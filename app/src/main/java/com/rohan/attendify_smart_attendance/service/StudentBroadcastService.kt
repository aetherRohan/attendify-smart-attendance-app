package com.rohan.attendify_smart_attendance.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.rohan.attendify_smart_attendance.data.ble.BleBroadcastClient
import com.rohan.attendify_smart_attendance.domain.session.StudentSessionController
import com.rohan.attendify_smart_attendance.utils.NotificationHelper
import kotlinx.coroutines.*

class StudentBroadcastService : Service() {


    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var studentSessionController= StudentSessionController()

    companion object {
        const val ACTION_START_BROADCAST = "ACTION_START"
        const val ACTION_STOP_BROADCAST = "ACTION_STOP"
        const val EXTRA_STUDENT_ID = "STUDENT_ID"
        private const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()
        val bleBroadcast = BleBroadcastClient(this, scope = serviceScope)
        studentSessionController.initialize(bleBroadcast = bleBroadcast, scope = serviceScope)
        Log.d("StudentService", "✅ Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val studentId = intent?.getStringExtra(EXTRA_STUDENT_ID)

        if (studentId.isNullOrEmpty()){
            Log.e("StudentService", "❌ Error: No Student ID provided.")
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundServicePromotion(isBroadcasting = true)

        studentSessionController.startAttendance(classId = " ", studentId = studentId)

        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("StudentService", "🛑 Service Destroyed")
       studentSessionController.stopAttendance()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServicePromotion(isBroadcasting: Boolean) {

        val notification = NotificationHelper.createAttendanceNotification(
            context = this,
            isTeacher = false,
            statusText = "Marking Your attendance"
        )
        try {
            if (Build.VERSION.SDK_INT >= 34) {
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
}