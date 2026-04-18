package com.rohan.attendify_smart_attendance.repository

import android.util.Log
import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.dao.PendingSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.StudentRosterDao
import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity
import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class TeacherSessionRepository(
    private val api: ApiService,
    private val rosterDao: StudentRosterDao,
    private val pendingSessionDao: PendingSessionDao
) {
    private val _sessionStatus = MutableStateFlow(TeacherSessionController.SessionStatus())
    val sessionStatus = _sessionStatus.asStateFlow()

    val TAG="pendingSession"


    //
    fun updateStatus(newStatus: TeacherSessionController.SessionStatus) {
        _sessionStatus.value = newStatus
    }

    //  fetch from the server and save to Room right here!
    suspend fun fetchAndSaveRoster(classId: String) {
        try {
            // 1. Fetch from Spring Boot
            val response = api.getClassRoster(classId)

            if (response.isSuccessful && response.body() != null) {
                // 2. Convert DTOs to Room Entities
                val roomEntities = response.body()!!.map { it.toRoomEntity() }

                // 3. Save to your local offline vault
                rosterDao.insertRoster(roomEntities)
                Log.i("session","StudentRoster fetched from server and saved to db")
            } else {
                Log.e("session", "Could not fetch the studentRoster error code:${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("session", "${e.message}")
        }
    }

    suspend fun getStudentsForClass(classId: String): List<StudentRosterEntity> {
        Log.e("session", "trying to fetch student from db")
        return try {
            rosterDao.getStudentsForClass(classId)
        } catch (e: Exception) {

            Log.e("session", "CRASH IN DB READ: ${e.message}", e)
            emptyList()
        }
    }



    suspend fun recordCurrentWindowAttendance(
        classId: String,
        windowIndex: Int,
        scannedStudents: List<String>
    ) {
        withContext(Dispatchers.IO) {
            try {
                // 2. Date is safely generated here, so we don't need it in the parameters!
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Query local Room DB
                val existingSession = pendingSessionDao.getSessionForToday(classId, todayDate)

                if (existingSession == null) {
                    // SCENARIO 1: First scan of the day. Initialize map with 1 hit per student.
                    val initialHits = scannedStudents.associateWith { 1 }

                    val newSession = PendingSessionEntity(
                        classId = classId,
                        sessionStartDate = todayDate,
                        totalWindows = windowIndex,
                        studentHitsMap = initialHits
                    )

                    pendingSessionDao.insertPendingSession(newSession)
                    Log.i(
                        TAG,
                        "Created new session for $todayDate. Initialized ${scannedStudents.size} students."
                    )

                } else {
                    // SCENARIO 2: Session exists. Safely increment the hit counts.
                    val updatedHits = existingSession.studentHitsMap.toMutableMap()

                    for (studentId in scannedStudents) {
                        val currentHits = updatedHits[studentId] ?: 0
                        updatedHits[studentId] = currentHits + 1
                    }

                    val updatedSession = existingSession.copy(
                        studentHitsMap = updatedHits,
                        totalWindows = windowIndex
                    )

                    pendingSessionDao.updateSession(updatedSession)

                    Log.i(
                        TAG,
                        "Updated session for $todayDate. Total windows now: ${updatedSession.totalWindows}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record window attendance safely: ${e.message}", e)
            }
        }
    }





 }

