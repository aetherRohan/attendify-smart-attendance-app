package com.rohan.attendify_smart_attendance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rohan.attendify_smart_attendance.ui.auth.AuthViewModel
import com.rohan.attendify_smart_attendance.ui.auth.AuthViewModelFactory
import com.rohan.attendify_smart_attendance.ui.root.DashBoardActivity
import com.rohan.attendify_smart_attendance.ui.root.RootScreen
import com.rohan.attendify_smart_attendance.ui.theme.AttendifySmartAttendanceTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        val app = application as AttendifyApplication
        AuthViewModelFactory(app.authRepository,app.tokenManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AttendifySmartAttendanceTheme {
                RootScreen(
                    authViewModel = authViewModel,
                    onNavigateToDashBoard = { name, role, id ->
                        navigateTo(DashBoardActivity::class.java, name, role, id)
                    }
                )
            }
        }
    }


    private fun navigateTo(activityClass: Class<*>, name: String, role: String, id: String) {
        val intent = Intent(this, activityClass).apply {
            putExtra("USER_NAME", name)
            putExtra("USER_ROLE", role)
            putExtra("USER_ID", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}