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

    fun createAttendanceNotification(
        context: Context,
        isTeacher: Boolean,
        statusText: String
    ): Notification {

        //Create Channel
        createChannel(context)

        // Determine Title based on Role
        val title = if (isTeacher) "Class Session Active" else "Attendance Active"

        // 3. Build Notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true) // User cannot swipe away
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority = no popup overlay
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // Create/Update channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low = No sound/popup
            ).apply {
                description = "Shows when an attendance session is active"
            }
            manager.createNotificationChannel(channel)
        }
    }
}