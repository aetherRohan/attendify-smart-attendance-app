package com.rohan.attendify_smart_attendance.ui.teacher.classDetails

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.attendify_smart_attendance.ui.components.ClassInfoCard
import com.rohan.attendify_smart_attendance.ui.components.TeacherClassSessionCard
import com.rohan.attendify_smart_attendance.ui.components.ScanBottomSheet
import com.rohan.attendify_smart_attendance.ui.components.debounced
import com.rohan.attendify_smart_attendance.ui.theme.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassDetailScreen(
    userName: String?,
    userId: String?,
    classId: String?,
    className: String?,
    duration: String?,
    section: String?,
    classCode: String?,
    viewModel: TeacherClassDetailViewModel,
    onStartScanClick: (Boolean, String) -> Unit,
    onClassSessionCardCLick: (String, String, String, String, String) -> Unit,
    onBackClick: () -> Unit = {} // Added for top bar navigation
) {
    // State for the Bottom Sheet
    var showScanningSheet by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()

    val classSessionList by viewModel.classSessionState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.syncDashboardData()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Class Details", fontWeight = FontWeight.Bold) },
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

            ClassInfoCard(
                className=className,
                duration = duration,
                classCode = classCode,
                section = section
            )

            Spacer(modifier = Modifier.height(26.dp))
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
                items(classSessionList, key = { it.classSessionId }) { currentSession ->
                    TeacherClassSessionCard(
                        date = currentSession.date,
                        onClick = {
                            onClassSessionCardCLick(
                                currentSession.classSessionId,
                                currentSession.date, currentSession.classId, className?:"",section?:""
                            )
                        }
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


