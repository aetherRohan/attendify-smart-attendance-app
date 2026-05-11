package com.rohan.attendify_smart_attendance.ui.teacher

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.attendify_smart_attendance.ui.components.ClassSessionCard
import com.rohan.attendify_smart_attendance.ui.components.ScanBottomSheet
import com.rohan.attendify_smart_attendance.ui.components.debounced
import com.rohan.attendify_smart_attendance.ui.theme.*


// --- Temporary Mock Data
data class MockSession(val id: String, val serialNumber: Int, val date: String, val presentCount: Int)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassDetailScreen(
    name: String,
    userId: String?,
    classId: String?,
    viewModel: TeacherClassDetailViewModel,
    onStartScanClick: (Boolean, String) -> Unit,
    onClassSessionCardCLick: () -> Unit,
    onBackClick: () -> Unit = {} // Added for top bar navigation
) {
    // State for the Bottom Sheet
    var showScanningSheet by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()


    // Mock Data for UI Preview (Replace with viewModel.pastSessions.collectAsState() etc.)
    val pastSessions = listOf(
        MockSession("3", 3, "Oct 26, 2024 • 10:00 AM", 42),
        MockSession("2", 2, "Oct 24, 2024 • 10:00 AM", 45),
        MockSession("1", 1, "Oct 22, 2024 • 10:00 AM", 44)
    ).sortedByDescending { it.serialNumber } // Ensures New to Old sorting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Class Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF8F9FA) // Very light gray background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

           // Start Class
            Button(
                onClick = debounced {
                    showScanningSheet = true
                    if (classId != null) {
                        onStartScanClick(true, classId) // Trigger the actual Bluetooth service
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AttendifyBlue),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Start Attendance Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Past Sessions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(pastSessions, key = { it.id }) { session ->
                    ClassSessionCard(
                        serialNumber = session.serialNumber,
                        date = session.date,
                        presentCount = session.presentCount,
                        onClick = onClassSessionCardCLick
                    )
                }
            }
        }
    }


    if (showScanningSheet) {
        ScanBottomSheet(
            state.studentsList,
            onDismiss = {
                showScanningSheet = false
                if (classId != null) {
                    onStartScanClick(false, classId) // Stop the scan
                }
            }
        )
    }
}


