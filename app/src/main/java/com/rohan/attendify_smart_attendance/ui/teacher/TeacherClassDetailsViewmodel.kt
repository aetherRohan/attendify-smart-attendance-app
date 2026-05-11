package com.rohan.attendify_smart_attendance.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class TeacherClassDetailViewModel(
    private val repository: TeacherSessionRepository
) : ViewModel() {

    val uiState: StateFlow<TeacherUiState> = repository.sessionStatus
        .map { status ->
            TeacherUiState(
                isScanning = status.isRunning,
                numberOfStudent = status.studentsFoundCount,
                studentsList = status.studentList,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TeacherUiState()
        )
}

data class TeacherUiState(
    val isScanning: Boolean = false,
    val numberOfStudent: Int = 0,
    val studentsList: List<String> = emptyList()
)

class TeacherViewModelFactory(
    private val repository: TeacherSessionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeacherClassDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeacherClassDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}