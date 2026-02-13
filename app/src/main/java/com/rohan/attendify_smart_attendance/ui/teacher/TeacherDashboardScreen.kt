package com.rohan.attendify_smart_attendance.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TeacherDashboardScreen(
    name: String,
    viewModel: TeacherDashboardViewModel,
    onToggle: (Boolean) -> Unit
){
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // CHANGED: Top alignment so items start from top, pushing list down
        verticalArrangement = Arrangement.Top
    ) {
        // 1. WELCOME HEADER
        Text(
            text = "Welcome $name",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. THE MAIN BUTTON (Start/Stop)
        Button(
            onClick = { onToggle(state.isScanning) },
            modifier = Modifier.size(160.dp), // Increased size slightly for better touch
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isScanning) Color.Red else Color(0xFF4CAF50)
            )
        ) {
            Text(state.buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. STATUS & COUNT
        Text(
            text = "Status: ${state.statsMessage}",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Text(
            text = "Total Found: ${state.numberOfStudent}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4. LIST HEADER
        Text(
            text = "Students in the Class",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start) // Align header to left
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 5. THE LIST (Fills remaining space)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // <--- CRITICAL: This makes the list expand to fill space
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.studentsList) { studentName ->
                Text(
                    text = studentName,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    fontSize = 18.sp
                )
                HorizontalDivider()
            }
        }
    }
}