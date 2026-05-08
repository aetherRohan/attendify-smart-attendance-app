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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TeacherDashboardScreen(
    name: String,
    viewModel: TeacherDashboardViewModel,
        onToggle: (Boolean) -> Unit
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Welcome $name",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))


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


        Text(
            text = "Students in the Class",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))


        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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