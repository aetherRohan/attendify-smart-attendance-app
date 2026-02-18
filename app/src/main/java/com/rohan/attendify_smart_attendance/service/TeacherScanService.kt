package com.rohan.attendify_smart_attendance.service

import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.rohan.attendify_smart_attendance.data.ble.BleScanClient
import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import com.rohan.attendify_smart_attendance.utils.NotificationHelper // <--- Import this
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class TeacherScanService : Service(){
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var teacherSessionController : TeacherSessionController?=null

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bleClient = BleScanClient(bluetoothManager.adapter, serviceScope)
        teacherSessionController= TeacherSessionController(bleClient = bleClient, sessionScope = serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // --- CLEANER: Delegate to Helper ---
        startForegroundServicePromotion()

        teacherSessionController?.startSession()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        teacherSessionController?.stopSession("0000180A-0000-1000-8000-00805F9B34FB")
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