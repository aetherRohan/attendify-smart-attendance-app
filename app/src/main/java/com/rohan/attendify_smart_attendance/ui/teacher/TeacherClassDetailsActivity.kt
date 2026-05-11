package com.rohan.attendify_smart_attendance.ui.teacher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.service.TeacherScanService
import com.rohan.attendify_smart_attendance.utils.PermissionManager
import kotlin.getValue

class TeacherClassDetailsActivity: ComponentActivity() {
    private val viewModel: TeacherClassDetailViewModel by viewModels {
        val app = application as AttendifyApplication
        TeacherViewModelFactory(app.teacherRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra("USER_NAME") ?: "Teacher"
        val role = intent.getStringExtra("USER_ROLE")
        val id = intent.getStringExtra("USER_ID")
        val classId = intent.getStringExtra("EXTRA_CLASS_ID")
        val className = intent.getStringExtra("CLASS_NAME")
        val duration = intent.getStringExtra("CLASS_DURATION")
        val section = intent.getStringExtra("CLASS_SECTION")
        val classCode = intent.getStringExtra("CLASS_CODE")

        setContent {
            TeacherClassDetailScreen(
                name = name,
                userId = id,
                classId = classId,
                className = className,
                duration = duration,
                section = section,
                classCode = classCode,
                viewModel = viewModel,
                onStartScanClick = { isCurrentlyScanning,classId ->
                    Log.i("teachService","button clicked start/stop")
                    handleToggle(isCurrentlyScanning,classId=classId)
                },
                onClassSessionCardCLick = {
                    //TODO OPEN CLASS_SESSION DETAIL SCREEN
                    // FOR TEACHER SHOW ALL THE STUDENTS WHO ARE PRESENT OR ABSENT
                },
                onBackClick = {
                    finish()
                }
            )

        }
    }

    private fun handleToggle(isScanning: Boolean, classId: String) {

        try {
            if (!PermissionManager.hasBluetoothPermissions(this)) {
                PermissionManager.requestBluetoothPermissions(this, 101)
                return
            }
            val intent = Intent(this, TeacherScanService::class.java).apply {
                putExtra("EXTRA_CLASS_ID", classId)
            }
            if (isScanning) {
                // Start the foreground scan service
                Log.i("teachService", "starting foreground service")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i("teachService", "starting teach service class intent 1")
                    startForegroundService(intent)
                } else {
                    Log.i("teachService", "starting teach service class intent 2")
                    startService(intent)
                }
            } else {
                Log.i("teachService", "stopping scan service")
                // Stop the service
                stopService(intent)
            }
        } catch (e: Exception) {

            e.printStackTrace()
            Log.e("teachService", "${e.message}")
        }
    }



}