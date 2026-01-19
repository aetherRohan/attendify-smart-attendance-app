package com.rohan.attendify_smart_attendance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun AnimatedTagline(
    modifier: Modifier = Modifier, textColor: Color = Color(0xFF546E7A)
) {
    val quotes = listOf(
        "Smart Attendance App", "Mark Attendance in Seconds", "Effortless & Paperless"
    )
    var quoteIndex by remember { mutableIntStateOf(0) }
    val revealProgress = remember { Animatable(0f) }

    LaunchedEffect(quoteIndex) {
        revealProgress.animateTo(
            targetValue = 1f, animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
        delay(2000)
        //  Hide (Reverse animation)
        revealProgress.animateTo(
            targetValue = 0f, animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
        quoteIndex = (quoteIndex + 1) % quotes.size
    }
    Box(
        modifier = modifier
            .wrapContentSize()
            .clipToBounds(),
        contentAlignment = Alignment.CenterStart
    ) {
        val currentText = quotes[quoteIndex]

        BoxWithReveal(
            text = currentText, progress = revealProgress.value, color = textColor
        )
    }
}
@Composable
fun BoxWithReveal(
    text: String, progress: Float, color: Color
) {
    Box(contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.drawWithClip(progress),
            textDecoration = TextDecoration.Underline,
        )
        // The Moving Line (Cursor)
        if (progress > 0.01f && progress < 0.99f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val lineWidth = 2.dp.toPx()
                val xPos = size.width * progress

                drawLine(
                    color = color,
                    start = Offset(xPos, 0f),
                    end = Offset(xPos, size.height),
                    strokeWidth = lineWidth
                )
            }
        }
    }
}

fun Modifier.drawWithClip(progress: Float): Modifier {
    return this.drawWithContent {
        clipRect(
            left = 0f, top = 0f, right = size.width * progress, bottom = size.height
        ) {
            this@drawWithContent.drawContent()
        }
    }
}