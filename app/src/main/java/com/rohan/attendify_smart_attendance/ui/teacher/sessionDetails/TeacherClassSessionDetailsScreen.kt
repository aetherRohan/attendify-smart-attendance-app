package com.rohan.attendify_smart_attendance.ui.teacher.sessionDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.attendify_smart_attendance.ui.components.SessionSummaryCard
import com.rohan.attendify_smart_attendance.ui.components.StudentAttendanceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassSessionDetailsScreen(
    className: String?,
    classSection: String?,
    classId: String?,
    classSessionId: String?,
    classSessionDate: String?,
    viewModel: TeacherClassSessionDetailsViewModel,
    onBackClick: () -> Unit
) {

    val attendanceList by viewModel.attendanceListState.collectAsState()

    val presentCount = remember(attendanceList) {
        attendanceList.count { it.isPresent }
    }
    val absentCount = remember(attendanceList, presentCount) {
        attendanceList.size - presentCount
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF8F9FA) // Light Gray Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // --- HEADER SUMMARY CARD ---
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Now passing the actual variables with fallbacks to prevent crashes
                SessionSummaryCard(
                    className = className ?: "Unknown Class",
                    classSection = classSection ?: "",
                    sessionDate = classSessionDate ?: "No Date Provided",
                    presentCount = presentCount,
                    absentCount = absentCount,
                    totalStudents = attendanceList.size
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Student Attendances",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- STUDENT ROSTER LIST ---
            items(items = attendanceList, key = { it.studentId }) { student ->
                StudentAttendanceItem(student = student)
            }
        }
    }
}