package com.rohan.attendify_smart_attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohan.attendify_smart_attendance.ui.theme.AttendifyBlue
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
            containerColor = MaterialTheme.colorScheme.background // 👈 changed from Color.White
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
            Text("Join Class", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
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
    val configuration = LocalConfiguration.current
    val drawerWidth = configuration.screenWidthDp.dp * 0.75f

    ModalDrawerSheet(
        modifier = Modifier.width(drawerWidth),
        drawerContainerColor = MaterialTheme.colorScheme.surface // 👈 changed from Color.White
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AttendifyPurple, AttendifyBlue)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = AttendifyPurple,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxHeight()
            ) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "All Classes") },
                    label = { Text("All Classes", fontWeight = FontWeight.Bold) },
                    selected = true,
                    onClick = { },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = roleColor.copy(alpha = 0.1f),
                        selectedTextColor = roleColor,
                        selectedIconColor = roleColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text("Attendance Reports", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("My Profile", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.HelpOutline, contentDescription = "Help") },
                    label = { Text("Help & Support", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Log Out") },
                    label = { Text("Log Out", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = onLogoutClick,
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color.Red.copy(alpha = 0.8f),
                        unselectedTextColor = Color.Red.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}