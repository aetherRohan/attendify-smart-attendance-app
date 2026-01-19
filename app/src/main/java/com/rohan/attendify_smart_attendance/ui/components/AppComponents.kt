package com.rohan.attendify_smart_attendance.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohan.attendify_smart_attendance.R
import com.rohan.attendify_smart_attendance.ui.theme.AttendifyPurple
import java.time.format.TextStyle

@Composable
fun AttendifyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    name: String = "Attendify",
    iconColor: Color?= AttendifyPurple
) {
    // 1. The "Pop" Gradient: Uses your App's specific Theme Colors
    // Blends from deep Purple -> Blue -> bright Green
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6200EA), // Deep Purple (Brand)
            Color(0xFF2196F3), // Attendify Blue (Teacher)
            Color(0xFF4CAF50)  // Attendify Green (Student)
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Attendify App Logo",
            modifier = Modifier.size(size),
            colorFilter = if (iconColor != null) ColorFilter.tint(iconColor) else null
        )

        Spacer(modifier = Modifier.width(12.dp))

        if (name.isNotEmpty()) {
            Text(
                text = name,
                style = androidx.compose.ui.text.TextStyle(
                    brush = gradientBrush,

                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
