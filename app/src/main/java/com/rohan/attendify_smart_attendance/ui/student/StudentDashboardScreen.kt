package com.rohan.attendify_smart_attendance.ui.student
import androidx.compose.foundation.layout.*

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
fun StudentDashboardScreen(
    name: String,
    viewModel: StudentDashboardViewmodel,
    onToggle: (Boolean) -> Unit
){
    val state by viewModel.uiState.collectAsState()

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
            onClick = { onToggle(state.isBroadcasting) },
            modifier = Modifier.size(260.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isBroadcasting) Color.Red else Color(0xFF4CAF50)
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
        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
    }
}