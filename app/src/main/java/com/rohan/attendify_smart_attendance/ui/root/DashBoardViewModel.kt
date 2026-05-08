package com.rohan.attendify_smart_attendance.ui.root

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.repository.StudentSessionRepository
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch


class DashBoardViewModel(
    private val userRole: String,
    private val teacherRepo: TeacherSessionRepository,
    private val studentRepo: StudentSessionRepository
) : ViewModel() {

    private val isTeacher = userRole.contains("TEACHER", ignoreCase = true)



    // need to call from student repo in the else block but for testing added only teacher repo
    val classListState: StateFlow<List<ClassEntity>> = (if (isTeacher) teacherRepo.getLocalClassesFlow()
                                                              else teacherRepo.getLocalClassesFlow())

        .catch { e ->
            Log.e("DashBoardViewModel", "Database Error: ${e.message}", e)
            emit(emptyList<ClassEntity>())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()


    // This ONLY talks to Retrofit. When Retrofit saves any new class to Room,

    fun syncDashboardData() {
        viewModelScope.launch {
            _isSyncing.value = true // Show loading spinner

            try {
                if (isTeacher) {

                   teacherRepo.syncAllTeacherData()
                } else {
                     //studentRepo.syncStudentDashboard()
                }
            } catch (e: Exception) {

                Log.e("DashBoardViewModel", "Network Sync Failed: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }
}


class DashboardViewModelFactory(
    private val userRole: String,
    private val teacherRepo: TeacherSessionRepository,
    private val studentRepo: StudentSessionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashBoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashBoardViewModel(userRole =  userRole,teacherRepo,studentRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
