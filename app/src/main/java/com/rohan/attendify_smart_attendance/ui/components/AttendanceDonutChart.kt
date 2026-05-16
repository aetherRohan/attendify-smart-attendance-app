package com.rohan.attendify_smart_attendance.ui.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun AttendanceDonutChart(present: Int, absent: Int, modifier: Modifier = Modifier) {
    val total = present + absent
    val presentPercentage = if (total == 0) 0f else (present.toFloat() / total) * 360f
    val absentPercentage = 360f - presentPercentage
    val percentageText = if (total == 0) 0 else (present * 100) / total

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()

            // Background Arc (Absent - Red)
            drawArc(
                color = Color(0xFFFEE2E2), // Very Light Red background
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Absent Arc (Red)
            drawArc(
                color = Color(0xFFF43F5E),
                startAngle = -90f + presentPercentage,
                sweepAngle = absentPercentage,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Present Arc (Green)
            drawArc(
                color = Color(0xFF10B981),
                startAngle = -90f,
                sweepAngle = presentPercentage,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentageText%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF334155)
            )
            Text(
                text = "Present",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}