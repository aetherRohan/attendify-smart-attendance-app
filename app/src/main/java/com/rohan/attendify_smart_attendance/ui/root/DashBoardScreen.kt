package com.rohan.attendify_smart_attendance.ui.root

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.rohan.attendify_smart_attendance.ui.theme.*
import com.rohan.attendify_smart_attendance.ui.components.*

@Composable
fun DashboardScreen(
    userName: String,
    userRole: String,
    userId: String,
    viewModel: DashBoardViewModel,
    onLogout: () -> Unit
) {

    LaunchedEffect(Unit) {
        viewModel.syncDashboardData()
    }

    val classList by viewModel.classListState.collectAsStateWithLifecycle()


    val isTeacher = userRole.contains("TEACHER", ignoreCase = true)
    val primaryRoleColor = if (isTeacher) AttendifyBlue else AttendifyGreen
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                userName = userName,
                userRole = if (isTeacher) "Teacher" else "Student",
                roleColor = primaryRoleColor,
                onLogoutClick = onLogout
            )
        }
    ) {
        Scaffold(
            topBar = {
                AttendifyTopBar(
                    roleColor = primaryRoleColor,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            floatingActionButton = {
                DynamicFab(
                    isTeacher = isTeacher,
                    roleColor = primaryRoleColor,
                    onClick = {
                        if (isTeacher) {
                            /// TODO: Open "Create Class" BottomSheet
                        } else {
                            /// TODO: Open "Join Class" Dialog
                        }
                    }
                )
            },
//            containerColor = Backgroundwhite
        ) { paddingValues ->
            // 4. The Main Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // For now, just a placeholder list. You will replace this with your actual ClassCards
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "My Classes",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }


                    if (classList.isEmpty()) {
                        item {
                            Text(
                                text = "No classes found. Add one to get started!",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    } else {
                        // Loop through the actual database data
                        items(classList.size) { index ->
                            val currentClass = classList[index]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {

                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = currentClass.className,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentClass.duration,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}