package com.rohan.attendify_smart_attendance.ui.student

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rohan.attendify_smart_attendance.data.local.entity.AttendanceEntity
import com.rohan.attendify_smart_attendance.repository.StudentSessionRepository
import com.rohan.attendify_smart_attendance.ui.teacher.sessionDetails.TeacherClassSessionDetailsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentClassDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: StudentSessionRepository
) : ViewModel() {

    private var _bleUuid = MutableStateFlow("")

    val bleUuid: StateFlow<String> = _bleUuid

    private val classId: String = savedStateHandle.get<String>("EXTRA_CLASS_ID") ?: ""
    private val studentId: String = savedStateHandle.get<String>("USER_ID") ?: ""


    init {
        viewModelScope.launch {
            _bleUuid.value = repository.getBleUuid()
        }
    }

    fun syncDashBoard(){
        viewModelScope.launch {
            try {
                repository.syncAllAttendance(classId = classId, studentId = studentId)
            }catch (e: Exception){
                Log.e("studentSession","failed to fetech data from server${e.message}")
            }
        }
    }


    //get data from db
    val attendanceListState: StateFlow<List<AttendanceEntity>> =
        repository.getLocalAttendanceFlow(classId = classId, studentId = studentId)

            .catch { e ->
                Log.e("studentSession", "Database Error: ${e.message}", e)
                emit(emptyList<AttendanceEntity>())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}


class StudentViewModelFactory(
    private val repository: StudentSessionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(StudentClassDetailViewModel::class.java)) {

            // Create the SavedStateHandle from the extras
            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return StudentClassDetailViewModel(savedStateHandle, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
