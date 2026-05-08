package com.rohan.attendify_smart_attendance.ui.student

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.service.StudentBroadcastService
import com.rohan.attendify_smart_attendance.utils.PermissionManager
import kotlinx.coroutines.launch
import kotlin.getValue

class StudentDashboardActivity: ComponentActivity() {
    private val viewModel: StudentDashboardViewmodel by viewModels(){
        val app =application as AttendifyApplication
        StudentViewModelFactory(app.studentRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra("USER_NAME") ?: "Student"
        setContent {
            StudentDashboardScreen(
                name=name,
                viewModel = viewModel,
                onToggle = { isBroadcasting,uuid ->
                    handleToggle(isBroadcasting,uuid)
                }
            )
        }
    }

    private fun handleToggle(isBroadcasting: Boolean,bleUuid: String) {
        Log.e("DEBUG_TEST", " Activity handleToggle reached")

        if (!PermissionManager.hasBluetoothPermissions(this)) {
            Log.e("DEBUG_TEST", "3. Permissions Missing - Requesting and Returning")
            PermissionManager.requestBluetoothPermissions(this, 101)
            return
        }

        val intent = Intent(this, StudentBroadcastService::class.java).apply {
            intent.putExtra("USER_BLE_UUID",bleUuid)
        }
        if (!isBroadcasting) {

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
    }

}