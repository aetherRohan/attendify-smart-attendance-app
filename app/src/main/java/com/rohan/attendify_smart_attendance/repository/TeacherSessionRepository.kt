package com.rohan.attendify_smart_attendance.repository

import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.dao.PendingSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.StudentRosterDao
import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 1. Change 'object' to 'class'
// 2. Pass your tools into the constructor
class TeacherSessionRepository(
    private val api: ApiService,
    private val rosterDao: StudentRosterDao,
    private val pendingSessionDao: PendingSessionDao
) {

    private val _sessionStatus = MutableStateFlow(TeacherSessionController.SessionStatus())
    val sessionStatus = _sessionStatus.asStateFlow()

    fun updateStatus(newStatus: TeacherSessionController.SessionStatus) {
        _sessionStatus.value = newStatus
    }

    // Now you can easily write your function to fetch from the server
    // and save to Room right here!
    suspend fun fetchAndSaveRoster(classId: String) {
        try {
            // 1. Fetch from Spring Boot
            val response = api.getClassRoster(classId)

            if (response.isSuccessful && response.body() != null) {
                // 2. Convert DTOs to Room Entities
                val roomEntities = response.body()!!.map { it.toRoomEntity() }

                // 3. Save to your local offline vault
                rosterDao.insertRoster(roomEntities)
            }
        } catch (e: Exception) {
            // Handle no internet connection
        }
    }
}