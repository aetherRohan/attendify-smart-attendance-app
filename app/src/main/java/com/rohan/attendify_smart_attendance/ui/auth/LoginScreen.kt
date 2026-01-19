package com.rohan.attendify_smart_attendance.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person // New Icon for Name
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohan.attendify_smart_attendance.R
import com.rohan.attendify_smart_attendance.model.UserRole
import com.rohan.attendify_smart_attendance.ui.components.AnimatedTagline
import com.rohan.attendify_smart_attendance.ui.components.AttendifyLogo
import com.rohan.attendify_smart_attendance.ui.theme.AttendifyBlue
import com.rohan.attendify_smart_attendance.ui.theme.AttendifyGreen

@Composable
fun LoginScreen(
    onAuthButtonClick: (String, String, UserRole, String, Boolean) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    var roleColor by remember { mutableStateOf(Color.Unspecified) }

    if (!isLoginMode) {
        if (selectedRole == UserRole.STUDENT) {
            roleColor = AttendifyGreen
        } else roleColor = AttendifyBlue
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .imePadding()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AttendifyLogo()
        Spacer(modifier = Modifier.height(13.dp))

        AnimatedTagline(
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(29.dp))
        Text(
            text = if (isLoginMode) "Welcome Back" else "Create Account",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLoginMode) MaterialTheme.colorScheme.primary else roleColor
        )
        Text(
            text = if (isLoginMode) "Login to your account" else "Sign up to get started",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))


        if (!isLoginMode) {

            RoleToggle(
                currentRole = selectedRole,
                onRoleSelected = { newRole -> selectedRole = newRole },
                roleColor
            )

            Spacer(modifier = Modifier.height(32.dp))


            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = "Toggle password")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = {
                onAuthButtonClick(email, password, selectedRole, name, isLoginMode)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoginMode) MaterialTheme.colorScheme.primary else roleColor
            )
        ) {
            Text(
                text = if (isLoginMode) "Login" else "Continue as ${selectedRole.name}",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- The Toggle Text Button ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                isLoginMode = !isLoginMode
            }
        ) {
            Text(
                text = if (isLoginMode) "Don't have an account? " else "Already have an account? ",
                color = Color.Black
            )
            Text(
                text = if (isLoginMode) "Sign Up" else "Login",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun RoleToggle(currentRole: UserRole, onRoleSelected: (UserRole) -> Unit, roleColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        @Composable
        fun RoleButton(role: UserRole, label: String) {
            val isSelected = currentRole == role
            val bgColor: Color = if (isSelected) roleColor else Color.Transparent
            val textColor = if (isSelected) Color.White else Color.Gray

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(bgColor)
                    .clickable { onRoleSelected(role) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        RoleButton(UserRole.STUDENT, "Student")
        RoleButton(UserRole.TEACHER, "Teacher")
    }
}