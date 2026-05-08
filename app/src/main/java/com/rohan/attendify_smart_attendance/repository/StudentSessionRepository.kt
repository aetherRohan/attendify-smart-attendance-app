package com.rohan.attendify_smart_attendance.repository

import android.util.Log
import com.rohan.attendify_smart_attendance.data.local.dao.ClassDao
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.domain.session.StudentSessionController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

class StudentSessionRepository(
   private val classDao: ClassDao,


) {

    private var _sessionStatus = MutableStateFlow(StudentSessionController.StudentSessionState())
    var sessionStatus = _sessionStatus.asStateFlow()
    fun updateStatus(newStatus: StudentSessionController.StudentSessionState) {
        _sessionStatus.value = newStatus
    }



    fun getLocalClassesFlow(): Flow<List<ClassEntity>>{

        return classDao.getAllClassesFlow()
            .onStart {
                Log.i("StudentRepo", "Started observing local classes DB")
            }
            .catch { e ->
                Log.e("StudentRepo", "Database read error: ${e.message}", e)
                // Emit an empty list to prevent the UI from crashing if the DB fails
                emit(emptyList())
            }
    }



}