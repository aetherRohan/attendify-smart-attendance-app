package com.rohan.attendify_smart_attendance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassBottomSheet(
    roleColor: Color,
    onDismiss: () -> Unit,
    onCreateClick: (className: String, section: String, duration: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var className by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create New Class",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Fill in the details below to generate a new class ",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("Class Name (e.g., Physics 101)") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = roleColor) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = roleColor, focusedLabelColor = roleColor)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section") },
                    leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = roleColor) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = roleColor, focusedLabelColor = roleColor)
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (Mins)") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = roleColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = roleColor, focusedLabelColor = roleColor)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onCreateClick(className, section, duration) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = className.isNotBlank() && duration.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = roleColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Class", fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }
        }
    }
}

@Composable
fun JoinClassDialog(
    roleColor: Color,
    onDismiss: () -> Unit,
    onJoinClick: (classCode: String) -> Unit
) {
    var classCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Join a Class",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the 7-digit code to join class.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = classCode,
                    onValueChange = {
                        // Force uppercase and limit to exactly 7 characters
                        if (it.length <= 7) classCode = it.uppercase()
                    },
                    label = { Text("Class Code") },
                    placeholder = { Text("e.g., A7B2X9P") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = roleColor, focusedLabelColor = roleColor),
                    textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onJoinClick(classCode) },
                enabled = classCode.length == 7,
                colors = ButtonDefaults.buttonColors(containerColor = roleColor)
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}