package com.rohan.attendify_smart_attendance.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.repository.StudentSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StudentDashbaordViewmodel: ViewModel(){
    val uiState: StateFlow<StudentUiState> = StudentSessionRepository.sessionStatus
        .map { status ->
            StudentUiState(
                isBroadcasting = status.isBroadcasting,
                buttonText = if (status.isBroadcasting) "END CLASS" else "MARK ATTENDANCE",
                statsMessage = if (status.isBroadcasting) "MARKING YOUR ATTENDANCE"
                else "Ready to Mark attendance"
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StudentUiState()
        )
}
data class StudentUiState(
    val isBroadcasting: Boolean = false,
    val buttonText: String = "MARK ATTENDANCE",
    val statsMessage: String = ""
)
