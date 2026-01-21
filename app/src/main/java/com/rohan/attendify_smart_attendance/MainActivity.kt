package com.rohan.attendify_smart_attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rohan.attendify_smart_attendance.ui.auth.LoginScreen
import com.rohan.attendify_smart_attendance.ui.theme.AttendifySmartAttendanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AttendifySmartAttendanceTheme {
                // In MainActivity.kt
                LoginScreen(
                    onAuthButtonClick = { email, password, role, name, isLogin ->


                    }
                )
            }
        }


    }
}