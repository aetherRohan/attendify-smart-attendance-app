package com.rohan.attendify_smart_attendance.ui.teacher.classDetails

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rohan.attendify_smart_attendance.data.local.entity.ClassSessionEntity
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class TeacherClassDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TeacherSessionRepository,
) : ViewModel() {

    private var classId: String?

    init {

        classId = savedStateHandle.get<String>("EXTRA_CLASS_ID")
        Log.i("classSession", "class id passed to viewmodel is :${classId}")
    }

    val classSessionState: StateFlow<List<ClassSessionEntity>> =
        repository.getLocalClassSessionFlow(classId = classId!!)

            .catch { e ->
                Log.e("classSession", "Database Error: ${e.message}", e)
                emit(emptyList<ClassSessionEntity>())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

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


    fun syncDashboardData() {
        viewModelScope.launch {
            try {
                Log.i("classSession", "fetching class-session data from server")
                repository.syncAllClassSessions(classId!!)

            } catch (e: Exception) {
                Log.e("classSession", "Network Sync Failed: ${e.message}")
            }
        }


    }
}

data class TeacherUiState(
    val isScanning: Boolean = false,
    val numberOfStudent: Int = 0,
    val studentsList: List<String> = emptyList()
)

class TeacherViewModelFactory(
    private val repository: TeacherSessionRepository
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(TeacherClassDetailViewModel::class.java)) {

            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return TeacherClassDetailViewModel(savedStateHandle, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}