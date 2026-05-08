package com.rohan.attendify_smart_attendance.ui.teacher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.service.TeacherScanService
import com.rohan.attendify_smart_attendance.utils.PermissionManager

class TeacherDashboardActivity : ComponentActivity() {

    private val viewModel: TeacherDashboardViewModel by viewModels {
        val app = application as AttendifyApplication
        TeacherViewModelFactory(app.teacherRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra("USER_NAME") ?: "Teacher"
        val role = intent.getStringExtra("USER_ROLE")
        val id = intent.getStringExtra("USER_ID")
        setContent {
            TeacherDashboardScreen(
                name=name,
                viewModel = viewModel,
                onToggle = { isCurrentlyScanning ->
                    handleToggle(isCurrentlyScanning)
                }
            )

        }
    }

    private fun handleToggle(isScanning: Boolean) {
        if (!PermissionManager.hasBluetoothPermissions(this)) {
            PermissionManager.requestBluetoothPermissions(this, 101)
            return
        }

        // Hard-Coded value for class id (TESTING ONLY)
        val intent = Intent(this, TeacherScanService::class.java).apply {
            putExtra("CLASS_ID","1")
        }
        if (!isScanning) {
            // Start the foreground scan service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } else {
            // Stop the service
            stopService(intent)
        }
    }
}