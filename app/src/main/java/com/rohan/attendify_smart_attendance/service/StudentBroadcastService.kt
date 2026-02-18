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
    private var studentSessionController: StudentSessionController?=null
    private val EXTRA_STUDENT_ID = "STUDENT_ID"
    private  val NOTIFICATION_ID = 101


    override fun onCreate() {
        super.onCreate()
        val bleBroadcast = BleBroadcastClient(this)
        studentSessionController=StudentSessionController(bleBroadcast = bleBroadcast, sessionScope = serviceScope)
        Log.d("StudentService", "✅ Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        val studentId = intent?.getStringExtra(EXTRA_STUDENT_ID)
        val studentId = "550e8400-e29b-41d4-a716-446655440000"

        if (studentId.isNullOrEmpty()){
            Log.e("StudentService", "❌ Error: No Student ID provided.")
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServicePromotion(isBroadcasting = true)

        studentSessionController?.startAttendance(classId = " ", studentId = studentId)

        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("StudentService", "🛑 Service Destroyed")
       studentSessionController?.stopAttendance()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServicePromotion(isBroadcasting: Boolean) {
        // 1. Ensure the Notification Channel is created BEFORE promotion
        // If the channel doesn't exist, startForeground fails silently.
        NotificationHelper.createAttendanceNotification(this,false,"Marking Attendance")

        val notification = NotificationHelper.createAttendanceNotification(
            context = this,
            isTeacher = false,
            statusText = if (isBroadcasting) "Marking Your Attendance..." else "Attendance Completed"
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+

                // 2. Android 14 double-check: Do we actually have the permission?
                // If we don't, this throws a SecurityException which we catch below.
                val hasPermission = checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    Log.e("StudentService", "❌ FATAL: BLUETOOTH_ADVERTISE permission missing for FGS")
                    // You might want to trigger a callback to the UI here to show a dialog
                    return
                }

                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29-33
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            Log.i("StudentService", "✅ startForeground successful")

        } catch (e: Exception) {
            // This will now catch the exact reason (e.g., "foregroundServiceType not declared in manifest")
            Log.e("StudentService", "❌ Foreground Error: ${e.message}")
            e.printStackTrace()
            stopSelf()
        }
    }
}