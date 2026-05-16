package com.rohan.attendify_smart_attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SessionSummaryCard(
    className: String,
    classSection: String, // Added classSection
    sessionDate: String,
    presentCount: Int,
    absentCount: Int,
    totalStudents: Int
) {
    // A subtle, modern Gen-Z aesthetic gradient (Light Blue to Light Indigo)
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEBF4FF),
            Color(0xFFF3F0FF)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Make transparent to show gradient
    ) {
        // Box handles the gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Class Name & Section dynamically combined
                val titleText = if (classSection.isNotBlank()) "$className - $classSection" else className

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A) // Deep Slate
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Session Date
                Text(
                    text = sessionDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Chart & Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Prevents overlap
                ) {
                    // Left: Dynamic Donut Chart
                    AttendanceDonutChart(
                        present = presentCount,
                        absent = absentCount,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp)) // Added buffer space

                    // Right: Stats Column
                    // using weight(1f) forces this column to stay on the right side without squishing the chart
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(label = "Total Students", value = totalStudents.toString(), dotColor = Color.Gray)
                        StatRow(label = "Present", value = presentCount.toString(), dotColor = Color(0xFF10B981))
                        StatRow(label = "Absent", value = absentCount.toString(), dotColor = Color(0xFFF43F5E))
                    }
                }
            }
        }
    }
}