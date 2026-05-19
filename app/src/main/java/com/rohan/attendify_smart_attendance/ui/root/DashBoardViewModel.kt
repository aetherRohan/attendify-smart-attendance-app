package com.rohan.attendify_smart_attendance.ui.root

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.domain.repository.StudentSessionRepository
import com.rohan.attendify_smart_attendance.domain.repository.TeacherSessionRepository
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

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()




    val classListState: StateFlow<List<ClassEntity>> = (if (isTeacher) teacherRepo.getLocalClassesFlow()
                                                              else studentRepo.getLocalClassesFlow())

        .catch { e ->
            Log.e("DashBoardViewModel", "Database Error: ${e.message}", e)
            emit(emptyList<ClassEntity>())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // This ONLY talks to Retrofit. When Retrofit saves any new class to Room,
    fun syncDashboardData() {
        viewModelScope.launch {
            try {
                if (isTeacher) {

                   teacherRepo.syncAllClasses()
                } else {
                     studentRepo.syncAllStudentClass()
                }
            } catch (e: Exception) {

                Log.e("DashBoardViewModel", "Network Sync Failed: ${e.message}")
            } finally {
            }
        }
    }

    fun createClass(className: String,section: String,duration: String) {
        viewModelScope.launch {

            _isSyncing.value = true
            try {
                teacherRepo.createClass(className,section,duration)
                Log.i("joinClass", "calling join class method from teacher repo")

            } catch (e: Exception) {

                e.printStackTrace()
                Log.e("joinClass", "${e.message}")

            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun joinClass(classCode: String) {
        viewModelScope.launch {

            _isSyncing.value = true
            try {
                studentRepo.joinClass(classCode)
                Log.i("joinClass", "calling join class method from student repo")

            } catch (e: Exception) {

                e.printStackTrace()
                Log.e("joinClass", "${e.message}")

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
