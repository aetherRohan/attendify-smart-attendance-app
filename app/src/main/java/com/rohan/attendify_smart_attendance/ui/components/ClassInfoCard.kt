package com.rohan.attendify_smart_attendance.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClassInfoCard(
    className: String?,
    classCode: String?,
    section: String?,
    duration: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        // The base color is kept white to ensure the shadow renders perfectly
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                // Add the light, subtle gradient background here so it fills the card
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEFF6FF), // Very Light Blue
                            Color(0xFFF5EEFF), // Very Light Purple
                            Color(0xFFFAF5FF)  // Very Light Violet
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // --- TOP ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top Left: Class Name
                Text(
                    text = className ?: "Unnamed Class",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5B1CC4),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )

                // Top Right: Section (Swapped here)
                if (!section.isNullOrEmpty()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Section", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = section,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTTOM ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Bottom Left: Class Code
                if (!classCode.isNullOrEmpty()) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Class Code", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF063E60),
                                            Color(0xFFD500F9)
                                        )
                                    )
                                )
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(classCode))
                                    Toast.makeText(context, "Class code copied!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = classCode,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Class Code",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Bottom Right: Duration (Swapped here)
                if (!duration.isNullOrEmpty()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Duration", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = "$duration min",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}