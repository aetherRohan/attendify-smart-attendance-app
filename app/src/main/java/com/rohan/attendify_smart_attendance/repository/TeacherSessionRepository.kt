package com.rohan.attendify_smart_attendance.repository

import android.util.Log
import androidx.sqlite.throwSQLiteException
import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.dao.PendingSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.StudentRosterDao
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity
import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeacherSessionRepository(
    private val api: ApiService,
    private val rosterDao: StudentRosterDao,
    private val pendingSessionDao: PendingSessionDao
) {
    private val _sessionStatus = MutableStateFlow(TeacherSessionController.SessionStatus())
    val sessionStatus = _sessionStatus.asStateFlow()


    //
    fun updateStatus(newStatus: TeacherSessionController.SessionStatus) {
        _sessionStatus.value = newStatus
    }

    //  fetch from the server and save to Room right here!
    suspend fun fetchAndSaveRoster(classId: String) {
        try {
            // 1. Fetch from Spring Boot
            val response = api.getClassRoster(classId,"")

            if (response.isSuccessful && response.body() != null) {
                // 2. Convert DTOs to Room Entities
                val roomEntities = response.body()!!.map { it.toRoomEntity() }

                // 3. Save to your local offline vault
                rosterDao.insertRoster(roomEntities)
                Log.i("session","~~~~data saved to local database ~~~~")
            }else{
                Log.e("session","${response.errorBody()} error code:${response.code()}")
            }
        } catch (e: Exception) {
           e.printStackTrace()
            Log.e("session","${e.message}")
        }
    }

    suspend fun getStudentsForClass(classId: String): List<StudentRosterEntity> {
        Log.e("session", "trying to fetch student from db")
        return try {
            rosterDao.getStudentsForClass(classId)
        } catch (e: Exception) {
            // THIS IS CRITICAL: It prints the exact reason it failed in red text!
            Log.e("session", "CRASH IN DB READ: ${e.message}", e)
            emptyList()
        }
    }




}