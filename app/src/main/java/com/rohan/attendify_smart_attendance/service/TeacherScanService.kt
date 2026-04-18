package com.rohan.attendify_smart_attendance.service

import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.data.ble.BleScanClient
import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import com.rohan.attendify_smart_attendance.utils.NotificationHelper // <--- Import this
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class TeacherScanService : Service(){
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var teacherSessionController : TeacherSessionController
    private lateinit var teacherRepository: TeacherSessionRepository
    private var classId: String? = null


    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bleClient = BleScanClient(bluetoothManager.adapter, serviceScope)
        val app = application as AttendifyApplication
        teacherRepository = app.teacherRepository

        teacherSessionController = TeacherSessionController(
            context = applicationContext,
            bleClient = bleClient,
            sessionScope = serviceScope,
            teacherRepository = teacherRepository
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        classId=intent?.getStringExtra("CLASS_ID")
        // --- CLEANER: Delegate to Helper ---
        startForegroundServicePromotion()

        classId?.let {
            teacherSessionController.startSession(it)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        teacherSessionController.stopSession()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServicePromotion() {

        val notification = NotificationHelper.createAttendanceNotification(this,
            isTeacher = true,
            statusText = "Marking Attendance")

        val notificationId = 102

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(notificationId, notification)
        }
    }
}