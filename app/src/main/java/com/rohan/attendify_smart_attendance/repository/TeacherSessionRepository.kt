package com.rohan.attendify_smart_attendance.repository

import android.util.Log
import androidx.room.withTransaction
import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.data.local.dao.ClassDao
import com.rohan.attendify_smart_attendance.data.local.dao.PendingSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.StudentRosterDao
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity
import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class TeacherSessionRepository(
    private val api: ApiService,
    private val database: AttendifyDatabase,
    private val rosterDao: StudentRosterDao,
    private val classDao: ClassDao,
    private val pendingSessionDao: PendingSessionDao
) {
    private val _sessionStatus = MutableStateFlow(TeacherSessionController.SessionStatus())
    val sessionStatus = _sessionStatus.asStateFlow()

    val TAG="pendingSession"


    //
    fun updateStatus(newStatus: TeacherSessionController.SessionStatus) {
        _sessionStatus.value = newStatus
    }


    suspend fun fetchAndSaveRoster(classId: String) {
        try {

            val response = api.getClassRoster(classId)

            if (response.isSuccessful && response.body() != null) {

                val roomEntities = response.body()!!.map { it.toRoomEntity() }


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

    fun getLocalClassesFlow(): Flow<List<ClassEntity>> {
        return classDao.getAllClassesFlow()
            .onStart {
                Log.i("TeacherRepo", "Started observing local classes DB")
            }
            .catch { e ->
                Log.e("TeacherRepo", "Database read error: ${e.message}", e)
                // Emit an empty list to prevent the UI from crashing if the DB fails
                emit(emptyList())
            }
    }


    suspend fun syncAllTeacherData() {
        coroutineScope {
            try {
                Log.i("TeacherRepo", "Starting Eager Sync for Teacher")

                // 1. Fetch Class List from Spring Boot
                val classResponse = api.getAllClasses()

                if (classResponse.isSuccessful && classResponse.body() != null) {
                    val classDtos = classResponse.body()!!

                    // 2. Fetch all rosters concurrently
                    val rosterJobs = classDtos.map { classDto ->
                        async {
                            val rosterResponse = api.getClassRoster(classDto.classId)
                            if (rosterResponse.isSuccessful) {
                                rosterResponse.body()?.map { studentDto ->

                                    studentDto.toRoomEntity()
                                } ?: emptyList()
                            } else {
                                Log.e("TeacherRepo", "Failed to fetch roster for ${classDto.classId}")
                                emptyList()
                            }
                        }
                    }
                    // Wait for all roster network calls to finish
                    val allStudents = rosterJobs.awaitAll().flatten()

                    val classEntities = classDtos.map { dto ->
                        dto.toRoomEntity()
                    }

                    //  Save everything atomically to Room to prevent data loss
                    database.withTransaction {
                        classDao.clearAllClasses()
                        rosterDao.clearAllStudents()

                        classDao.insertClasses(classEntities)
                        rosterDao.insertRoster(allStudents)
                    }

                    Log.i("TeacherRepo", "Successfully synced ${classEntities.size} classes and ${allStudents.size} students.")
                } else {
                    Log.e("TeacherRepo", "Server returned error: ${classResponse.code()}")
                }
            } catch (e: Exception) {
                // If there's no internet, this catch block catches the Retrofit exception.
                //the UI just keeps displaying the old Room data!
                Log.e("TeacherRepo", "Network Sync Failed. Working offline. Error: ${e.message}")
            }
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

