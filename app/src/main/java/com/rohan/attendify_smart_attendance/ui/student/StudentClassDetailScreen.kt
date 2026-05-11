package com.rohan.attendify_smart_attendance.ui.student

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohan.attendify_smart_attendance.ui.components.ActiveBroadcastBottomSheet
import com.rohan.attendify_smart_attendance.ui.components.ClassInfoCard
import com.rohan.attendify_smart_attendance.ui.components.StudentSessionCard
import com.rohan.attendify_smart_attendance.ui.components.debounced
import com.rohan.attendify_smart_attendance.ui.theme.*

data class StudentSessionRecord(val id: String, val date: String, val isPresent: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClassDetailScreen(
    name: String?,
    userId: String?,
    classId: String?,
    className: String?,
    duration: String?,
    section: String?,
    classCode: String?,
    bleUuid: String?,
    viewModel: StudentClassDetailViewModel,
    onStartBroadcastClick: (Boolean, String?) -> Unit,
    onBackClick: () -> Unit = {}
) {
    var showBroadcastingSheet by remember { mutableStateOf(false) }

    // Mock Data for UI Preview (Replace with viewModel state collection)
    val pastSessions = listOf(
        StudentSessionRecord("3", "Oct 26, 2024 • 10:00 AM", true),
        StudentSessionRecord("2", "Oct 24, 2024 • 10:00 AM", false),
        StudentSessionRecord("1", "Oct 22, 2024 • 10:00 AM", true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Class Details",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- DISTINCT CLASS INFORMATION SECTION ---
            ClassInfoCard(
                className = className,
                classCode = classCode,
                section = section,
                duration = duration
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN ACTION BUTTON ---
            Button(
                onClick = debounced {
                    showBroadcastingSheet = true
                    onStartBroadcastClick(true, bleUuid)
                    Log.i("DEBUS_TEST", "passed ble to function :$bleUuid")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AttendifyGreen),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Mark My Attendance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "My Attendance History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- HISTORY LIST ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(pastSessions, key = { it.id }) { session ->
                    StudentSessionCard(
                        date = session.date,
                        isPresent = session.isPresent
                    )
                }
            }
        }
    }

    // --- BOTTOM SHEET ---
    if (showBroadcastingSheet) {
        ActiveBroadcastBottomSheet(
            onDismiss = {
                showBroadcastingSheet = false
                onStartBroadcastClick(false, bleUuid)
            }
        )
    }
}