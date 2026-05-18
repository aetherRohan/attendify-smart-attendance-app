package com.rohan.attendify_smart_attendance.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.data.ble.BleBroadcastClient
import com.rohan.attendify_smart_attendance.domain.session.StudentSessionController
import com.rohan.attendify_smart_attendance.utils.NotificationHelper
import kotlinx.coroutines.*

class StudentBroadcastService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var studentSessionController: StudentSessionController? = null
    private val NOTIFICATION_ID = 101


    override fun onCreate() {
        super.onCreate()
        val bleBroadcast = BleBroadcastClient(this)
        val app = application as AttendifyApplication
        studentSessionController = StudentSessionController(
            bleBroadcast = bleBroadcast,
            sessionScope = serviceScope,
            studentRepo = app.studentRepository
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val bleUuid = intent?.getStringExtra("USER_BLE_UUID")

        if (bleUuid.isNullOrEmpty()) {
            Log.e("StudentService", " Error: No BLE_UUID provided.")
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundServicePromotion(isBroadcasting = true)

        studentSessionController?.startAttendance(bleUuid = bleUuid)

        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        studentSessionController?.stopAttendance()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServicePromotion(isBroadcasting: Boolean) {

        NotificationHelper.createAttendanceNotification(this, false, "Marking Attendance")

        val notification = NotificationHelper.createAttendanceNotification(
            context = this,
            isTeacher = false,
            statusText = if (isBroadcasting) "Marking Your Attendance..." else "Attendance Completed"
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+

                val hasPermission =
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADVERTISE) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    Log.e("StudentService", "BLUETOOTH_ADVERTISE permission missing for FGS")
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
        } catch (e: Exception) {
            Log.e("StudentService", "Foreground Error: ${e.message}")
            stopSelf()
        }
    }
}