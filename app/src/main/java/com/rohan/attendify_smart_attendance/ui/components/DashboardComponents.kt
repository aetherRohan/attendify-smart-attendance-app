package com.rohan.attendify_smart_attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.attendify_smart_attendance.ui.theme.AttendifyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendifyTopBar(
    onMenuClick: () -> Unit,
    roleColor: Color
) {
    TopAppBar(
        title = {
            Text(
                text = "Attendify",
                fontWeight = FontWeight.Bold,
                color = AttendifyPurple
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Drawer",
                    tint = roleColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun DynamicFab(
    isTeacher: Boolean,
    roleColor: Color,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = roleColor,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        if (isTeacher) {
            Icon(Icons.Default.Add, contentDescription = "Create Class")
        } else {
            // Assuming you have a custom link/join icon, using text or a standard icon here
            Text("Join", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DrawerContent(
    userName: String,
    userRole: String,
    roleColor: Color,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(roleColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    color = roleColor,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = userRole, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            NavigationDrawerItem(
                label = { Text("All Classes", fontWeight = FontWeight.Bold) },
                selected = true,
                onClick = { /* Handle click */ },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = roleColor.copy(alpha = 0.1f),
                    selectedTextColor = roleColor
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            TextButton(
                onClick = onLogoutClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Log Out")
            }
        }
    }
}