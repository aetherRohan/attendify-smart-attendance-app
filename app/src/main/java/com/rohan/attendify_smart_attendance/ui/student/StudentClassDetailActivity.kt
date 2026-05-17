package com.rohan.attendify_smart_attendance.ui.student

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.service.StudentBroadcastService
import com.rohan.attendify_smart_attendance.ui.theme.AttendifySmartAttendanceTheme
import com.rohan.attendify_smart_attendance.utils.PermissionManager
import kotlin.getValue

class StudentClassDetailActivity : ComponentActivity() {
    private val viewModel: StudentClassDetailViewModel by viewModels() {
        val app = application as AttendifyApplication
        StudentViewModelFactory(app.studentRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userName = intent.getStringExtra("USER_NAME") ?: "Student"
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val classId = intent.getStringExtra("EXTRA_CLASS_ID")
        val className = intent.getStringExtra("CLASS_NAME")
        val duration = intent.getStringExtra("CLASS_DURATION")
        val section = intent.getStringExtra("CLASS_SECTION")
        val classCode = intent.getStringExtra("CLASS_CODE")

        val bleUuid = viewModel.bleUuid.value
        Log.i("DEBUG_TEST", "fetched ble id from viewmodel :${bleUuid}")

        enableEdgeToEdge()
        setContent {
            AttendifySmartAttendanceTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudentClassDetailScreen(
                        name = userName,
                        userId = userId,
                        classId = classId,
                        className = className,
                        duration = duration,
                        section = section,
                        classCode = classCode,
                        bleUuid = bleUuid,
                        viewModel = viewModel,
                        onStartBroadcastClick = { isBroadcasting, bleUuid ->
                            if (bleUuid != null) handleToggle(isBroadcasting, bleUuid)
                            else Log.e("studentActivity", "Ble uuid is Null")
                        },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }

    private fun handleToggle(isBroadcasting: Boolean, bleUuid: String?) {
        Log.e("DEBUG_TEST", " Activity handleToggle reached")
        Log.e("DEBUG_TEST", " ble id in handle toggle function:${bleUuid}")

        try {
            if (!PermissionManager.hasBluetoothPermissions(this)) {
                Log.e("DEBUG_TEST", "3. Permissions Missing - Requesting and Returning")
                PermissionManager.requestBluetoothPermissions(this, 101)
                return
            }
            if (bleUuid == null) {
                Log.e("DEBUG_TEST", "No ble id found in the handle toggle function")
            }
            val intent = Intent(this, StudentBroadcastService::class.java).apply {
                putExtra("USER_BLE_UUID", bleUuid)
            }
            if (isBroadcasting) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e("DEBUG_TEST", "4. Permissions OK - Starting Service")
                    startForegroundService(intent)
                } else {
                    Log.e("DEBUG_TEST", "4. Permissions OK - Starting Service")
                    startService(intent)
                }
            } else {
                stopService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DEBUG_TEST", "${e.message}")
        }
    }
}