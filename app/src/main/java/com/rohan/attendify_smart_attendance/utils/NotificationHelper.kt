package com.rohan.attendify_smart_attendance.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rohan.attendify_smart_attendance.R

object NotificationHelper {

    private const val CHANNEL_ID = "AttendifyClassSession"
    private const val CHANNEL_NAME = "Active Class Session"

    fun createAttendanceNotification(context: Context): Notification {
        // 1. Create Channel (Safe to call repeatedly)
        createChannel(context)

        // 2. Build Notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Attendance Session Active")
            .setContentText("Scanning for students via Bluetooth...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this exists
            .setOngoing(true) // Persistent
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // Avoid recreating if it already exists
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW // Low = No sound/popup
                ).apply {
                    description = "Shows when a teacher is hosting a class"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}