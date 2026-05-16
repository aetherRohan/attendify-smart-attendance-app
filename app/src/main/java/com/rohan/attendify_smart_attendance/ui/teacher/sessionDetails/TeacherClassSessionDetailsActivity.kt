package com.rohan.attendify_smart_attendance.ui.teacher.sessionDetails

import android.os.Bundle
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
import com.rohan.attendify_smart_attendance.ui.teacher.sessionDetails.TeacherClassSessionDetailsViewModel.TeacherClassSessionViewModelFactory
import com.rohan.attendify_smart_attendance.ui.theme.AttendifySmartAttendanceTheme

class TeacherClassSessionDetailsActivity : ComponentActivity() {

    private val viewModel: TeacherClassSessionDetailsViewModel by viewModels {
        val app = application as AttendifyApplication
        TeacherClassSessionViewModelFactory(app.teacherRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("USER_ID")
        val classSessionId = intent.getStringExtra("CLASS_SESSION_ID")
        val classId = intent.getStringExtra("EXTRA_CLASS_ID")
        val className = intent.getStringExtra("CLASS_NAME")
        val section = intent.getStringExtra("CLASS_SECTION")
        val date = intent.getStringExtra("CLASS_SESSION_DATE")

        enableEdgeToEdge() // 👈
        setContent {
            AttendifySmartAttendanceTheme { // 👈
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars), // 👈
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeacherClassSessionDetailsScreen(
                        className = className,
                        classSection = section,
                        classId = classId,
                        classSessionId = classSessionId,
                        classSessionDate = date,
                        viewModel = viewModel,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}