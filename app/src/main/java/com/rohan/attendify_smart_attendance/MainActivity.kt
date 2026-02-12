package com.rohan.attendify_smart_attendance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.ui.auth.LoginRoute
import com.rohan.attendify_smart_attendance.ui.theme.AttendifySmartAttendanceTheme
import com.rohan.attendify_smart_attendance.ui.auth.AuthViewModel
import com.rohan.attendify_smart_attendance.ui.teacher.TeacherDashboardActivity

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AttendifySmartAttendanceTheme {

                LoginRoute(
                    viewModel = authViewModel,
                    onNavigateToHome = { data ->

                        val user = data as? LoginResponse

                        if (user != null) {
                            Toast.makeText(
                                this,
                                "Welcome back, ${user.name.substringBefore(" ")}!",
                                Toast.LENGTH_LONG
                            ).show()

                            // 1. Determine which Activity to launch based on role
                            val destinationActivity = if (user.role == "TEACHER") {
                                TeacherDashboardActivity::class.java
                            } else {
                                // This will launch your student dashboard once you build it
                                TeacherDashboardActivity::class.java
                            }

                            // 2. Create the Intent
                            val intent = Intent(this, destinationActivity).apply {
                                putExtra("USER_NAME", user.name)
//                                putExtra("USER_EMAIL", user.email)
                                // Add flags to prevent the user from going back to the Login screen
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }

                            // 3. Start Activity and Finish MainActivity
                            startActivity(intent)
                            finish()
                        } else {
                            // Fallback if casting fails
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}