package com.rohan.attendify_smart_attendance.ui.root

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rohan.attendify_smart_attendance.data.remote.dto.LoginResponse
import com.rohan.attendify_smart_attendance.ui.auth.AuthViewModel
import com.rohan.attendify_smart_attendance.ui.auth.LoginRoute
import com.rohan.attendify_smart_attendance.ui.auth.SessionState

@Composable
fun RootScreen(
    authViewModel: AuthViewModel,
    onNavigateToDashBoard:(name: String, role: String, userId: String)-> Unit
) {

    val sessionState by authViewModel.sessionState.collectAsState()

    when (val state = sessionState) {

        is SessionState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is SessionState.Authenticated -> {
            LaunchedEffect(state) {
                onNavigateToDashBoard(state.name,state.role,state.userId)
            }
        }

        is SessionState.Unauthenticated -> {
            LoginRoute(
                viewModel = authViewModel,
                onNavigateToHome = { data ->
                    val user = data as? LoginResponse
                    if (user != null && !user.role.isBlank()) {
                        onNavigateToDashBoard(user.name,user.role,user.userId)
                    }else{
                        Log.e("auth","missing data")
                    }
                }
            )
        }
    }
}