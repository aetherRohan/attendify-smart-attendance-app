package com.rohan.attendify_smart_attendance.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.repository.StudentSessionRepository
import com.rohan.attendify_smart_attendance.ui.teacher.TeacherDashboardViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StudentDashboardViewmodel(
    private val repository: StudentSessionRepository
): ViewModel(){
    val uiState: StateFlow<StudentUiState> = repository.sessionStatus
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


class StudentViewModelFactory(
    private val repository: StudentSessionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeacherDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentDashboardViewmodel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
