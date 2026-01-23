package com.rohan.attendify_smart_attendance

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.ui.auth.LoginRoute
import com.rohan.attendify_smart_attendance.ui.theme.AttendifySmartAttendanceTheme
import com.rohan.attendify_smart_attendance.viewModel.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AttendifySmartAttendanceTheme {

                LoginRoute(
                    viewModel = authViewModel,
                    onNavigateToHome = { data ->
                        // 3. CAST TO REAL DATA TYPE
                        // The 'data' comes as 'Any?', so we cast it to 'LoginResponse'
                        val user = data as? LoginResponse

                        // 4. SHOW REAL DATA
                        // We check if cast was successful to avoid crashes
                        if (user != null) {
                            Toast.makeText(
                                this,
                                // ✅ Using the real 'name' field from your DTO
                                "Welcome back, ${user.name.substringBefore(" ")}!",
                                Toast.LENGTH_LONG
                            ).show()
                            // === DASHBOARD NAVIGATION (Ready to uncomment) ===
                            // val intent = Intent(this, DashboardActivity::class.java)
                            // intent.putExtra("USER_NAME", user.name) // Pass data to next screen
                            // intent.putExtra("USER_ROLE", user.role)
                            // startActivity(intent)
                            // finish() // Destroys Login Activity
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