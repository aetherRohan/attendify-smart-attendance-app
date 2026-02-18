package com.rohan.attendify_smart_attendance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.ui.auth.LoginRoute
import com.rohan.attendify_smart_attendance.ui.theme.AttendifySmartAttendanceTheme
import com.rohan.attendify_smart_attendance.ui.auth.AuthViewModel
import com.rohan.attendify_smart_attendance.ui.student.StudentDashboardActivity
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
                        // 1. DEBUG LOGGING: See exactly what is coming back
                        Log.d("Signin", "Raw Data Received: $data")

                        // 2. Safe Casting
                        val user = data as? LoginResponse

                        if (user != null) {

                            Log.d("Signin", "Role: '${user.role}'")

                            val rawRole = user.role
                            val isTeacher = rawRole.contains("ROLE_TEACHER", ignoreCase = true)

                            val destinationActivity = if (isTeacher) {
                                TeacherDashboardActivity::class.java
                            } else {
                                StudentDashboardActivity::class.java
                            }

                            try {
                                val intent = Intent(this@MainActivity, destinationActivity).apply {
                                    putExtra("USER_NAME", user.name)
                                    putExtra("USER_ROLE", user.role)
                                    putExtra("STUDENT_ID", user.userId)

                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                                finish()
                            } catch (e: Exception) {
                                Log.e("Signin", "❌ CRITICAL: Activity Launch Failed. Did you add it to Manifest?", e)
                                Toast.makeText(this@MainActivity, "Error launching screen", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Log.e("Signin", "❌ Casting Failed. Data was not LoginResponse.")
                            Toast.makeText(this@MainActivity, "Login Error: Invalid Data", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}