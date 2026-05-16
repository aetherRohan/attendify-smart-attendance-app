package com.rohan.attendify_smart_attendance.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.attendify_smart_attendance.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanBottomSheet(
    studentList: List<String>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            newState != SheetValue.Hidden
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        ),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        BackHandler(enabled = true) {
            Toast.makeText(
                context,
                "Press the red button to end the session.",
                Toast.LENGTH_SHORT
            ).show()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Taking Attendance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Scanning via Bluetooth...",
                        color = AttendifyBlue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Stop Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(AttendifyRed.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.StopCircle, contentDescription = "Stop", tint = AttendifyRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Live Count Badge
            Surface(
                color = AttendifyGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${studentList.size} Students Scanned",
                    color = AttendifyGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of scanned students
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(studentList) { student ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AttendifyGreen
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(student, fontWeight = FontWeight.Bold)
                            // Text(student.rollNumber, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveBroadcastBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            newState != SheetValue.Hidden
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        ),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        // Intercept back navigation directly within the bottom sheet window
        BackHandler(enabled = true) {
            Toast.makeText(
                context,
                "Press the Stop button to cancel broadcasting.",
                Toast.LENGTH_SHORT
            ).show()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Marking Attendance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Broadcasting signal...",
                        color = AttendifyGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Stop Button
                IconButton(
                    onClick = debounced { onDismiss() },
                    modifier = Modifier.background(AttendifyRed.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.StopCircle, contentDescription = "Stop", tint = AttendifyRed)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Visual Indicator for Broadcasting
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(AttendifyGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "Broadcasting",
                    modifier = Modifier.size(64.dp),
                    tint = AttendifyGreen
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Please keep this screen open and remain near the teacher's device.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}