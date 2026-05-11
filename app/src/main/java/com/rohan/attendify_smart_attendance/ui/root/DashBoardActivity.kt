package com.rohan.attendify_smart_attendance.ui.root

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rohan.attendify_smart_attendance.AttendifyApplication
import com.rohan.attendify_smart_attendance.MainActivity
import com.rohan.attendify_smart_attendance.ui.auth.AuthViewModel
import com.rohan.attendify_smart_attendance.ui.auth.AuthViewModelFactory
import com.rohan.attendify_smart_attendance.ui.auth.SessionState
import com.rohan.attendify_smart_attendance.ui.student.StudentDashboardActivity
import com.rohan.attendify_smart_attendance.ui.teacher.TeacherClassDetailsActivity

class DashBoardActivity : ComponentActivity() {


    private val authViewModel: AuthViewModel by viewModels {
        val app = application as AttendifyApplication
        AuthViewModelFactory(app.authRepository, app.tokenManager)
    }

    private val viewModel: DashBoardViewModel by viewModels {
        val app=application as AttendifyApplication
        DashboardViewModelFactory(intent.getStringExtra("USER_ROLE")!!,app.teacherRepository,app.studentRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val name = intent.getStringExtra("USER_NAME") ?: "Teacher"
        val role = intent.getStringExtra("USER_ROLE")
        val id = intent.getStringExtra("USER_ID")

        super.onCreate(savedInstanceState)

        setContent {
            val sessionState by authViewModel.sessionState.collectAsState()

            LaunchedEffect(sessionState) {
                if (sessionState is SessionState.Unauthenticated) {

                    val intent = Intent(this@DashBoardActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish() // Destroy the Dashboard
                }
            }

            DashboardScreen(
                userName = name,
                userRole = role?:"",
                userId = id?:"",
                viewModel,
                onLogout = {
                    authViewModel.logout()
                },
                onClickOpenClassDetails = {isTeacher,name,userId,classId->
                    if (isTeacher)
                        navigateTo(TeacherClassDetailsActivity::class.java,name,userId,classId)
                    else
                        navigateTo(StudentDashboardActivity::class.java,name,userId,classId)
                }
            )
        }
    }

    private fun navigateTo(activityClass: Class<*>, name: String, userId: String,classId: String) {
        val intent = Intent(this, activityClass).apply {
            putExtra("USER_NAME", name)
            putExtra("EXTRA_CLASS_ID", classId)
            putExtra("USER_ID", userId)
        }
        startActivity(intent)
    }
}