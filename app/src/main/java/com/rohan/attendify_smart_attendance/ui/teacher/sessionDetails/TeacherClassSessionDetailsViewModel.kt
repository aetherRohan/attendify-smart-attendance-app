package com.rohan.attendify_smart_attendance.ui.teacher.sessionDetails

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewmodel.CreationExtras
import com.rohan.attendify_smart_attendance.data.local.entity.AttendanceEntity


class TeacherClassSessionDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TeacherSessionRepository,
) : ViewModel() {

    private var classSessionId: String?

    init {

        classSessionId = savedStateHandle.get<String>("CLASS_SESSION_ID")
        Log.i("classSession", "class session id passed to viewmodel is :${classSessionId}")
    }

    val attendanceListState: StateFlow<List<AttendanceEntity>> =
        repository.getLocalAttendanceFlow(classSessionId = classSessionId!!)

            .catch { e ->
                Log.e("DashBoardViewModel", "Database Error: ${e.message}", e)
                emit(emptyList<AttendanceEntity>())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )





    class TeacherClassSessionViewModelFactory(
        private val repository: TeacherSessionRepository
    ) : ViewModelProvider.Factory {

        // Override the correct create method that includes CreationExtras
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(TeacherClassSessionDetailsViewModel::class.java)) {

                // Create the SavedStateHandle from the extras
                val savedStateHandle = extras.createSavedStateHandle()

                @Suppress("UNCHECKED_CAST")
                return TeacherClassSessionDetailsViewModel(savedStateHandle, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
