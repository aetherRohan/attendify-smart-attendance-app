package com.rohan.attendify_smart_attendance.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
class TeacherDashboardViewModel : ViewModel() {
    // Transform the controller's status into a clean state for the UI
    val uiState: StateFlow<TeacherUiState> = TeacherSessionRepository.sessionStatus
        .map { status ->
            TeacherUiState(
                isScanning = status.isRunning,
                buttonText = if (status.isRunning) "STOP SESSION" else "START SESSION",
                numberOfStudent = status.studentsFoundCount,
                studentsList = status.studentList,
                statsMessage = if (status.isRunning) ""
                else "Ready to begin attendance"
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TeacherUiState()
        )
}
data class TeacherUiState(
    val isScanning: Boolean = false,
    val buttonText: String = "START SESSION",
    val statsMessage: String = "Initializing...",
    val numberOfStudent:Int=0 ,
    val studentsList: List<String> =emptyList()
)