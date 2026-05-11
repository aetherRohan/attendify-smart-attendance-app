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
    onLogout: () -> Unit,
    onClickOpenClassDetails:(Boolean, String, String, String)-> Unit
) {

    LaunchedEffect(Unit) {
        viewModel.syncDashboardData()
    }

    val classList by viewModel.classListState.collectAsStateWithLifecycle()

    var showCreateClassSheet by remember { mutableStateOf(false) }
    var showJoinClassDialog by remember { mutableStateOf(false) }

    val isTeacher = userRole.contains("TEACHER", ignoreCase = true)
    val primaryRoleColor = if (isTeacher) AttendifyBlue else AttendifyGreen
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    if (showCreateClassSheet){
        CreateClassBottomSheet(
            roleColor = primaryRoleColor,
            onDismiss = {
                showCreateClassSheet=false
            },
            onCreateClick = { className, section, duration ->

                viewModel.createClass(className,section,duration)
                showCreateClassSheet=false
            }
        )
    }

    if (showJoinClassDialog){
        JoinClassDialog(
            roleColor = primaryRoleColor,
            onDismiss = {
                showJoinClassDialog=false
            },
            onJoinClick = {classCode ->
                viewModel.joinClass(classCode)
                showJoinClassDialog=false
            }
        )
    }

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
                            showCreateClassSheet=true
                        } else {
                           showJoinClassDialog=true
                        }
                    }
                )
            },

        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. The Header
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

                        items(
                            count = classList.size,
                            key = { index -> classList[index].classId }
                        ) { index ->
                            val currentClass = classList[index]

                            ClassCard(
                                className = currentClass.className,
                                duration = currentClass.duration,
                                section = currentClass.section,
                                onClick = {
                                    onClickOpenClassDetails(isTeacher,userName,userId,currentClass.classId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}