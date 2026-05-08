package com.rohan.attendify_smart_attendance.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// 1. Pass the repository into the constructor!
class TeacherDashboardViewModel(
    private val repository: TeacherSessionRepository
) : ViewModel() {

    //  Read from the injected 'repository' variable
    val uiState: StateFlow<TeacherUiState> = repository.sessionStatus
        .map { status ->
            TeacherUiState(
                isScanning = status.isRunning,
                buttonText = if (status.isRunning) "STOP SESSION" else "START SESSION",
                numberOfStudent = status.studentsFoundCount,
                studentsList = status.studentList,
                statsMessage = if (status.isRunning) "" else "Ready to begin attendance"
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
    val numberOfStudent: Int = 0,
    val studentsList: List<String> = emptyList()
)

class TeacherViewModelFactory(
    private val repository: TeacherSessionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeacherDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeacherDashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}