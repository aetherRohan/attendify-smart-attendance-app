package com.rohan.attendify_smart_attendance.repository


import android.util.Log
import androidx.room.withTransaction
import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.data.local.dao.AttendanceDao
import com.rohan.attendify_smart_attendance.data.local.dao.ClassDao
import com.rohan.attendify_smart_attendance.data.local.dao.ClassSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.PendingSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.StudentRosterDao
import com.rohan.attendify_smart_attendance.data.local.entity.AttendanceEntity
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.data.local.entity.ClassSessionEntity
import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity
import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import com.rohan.attendify_smart_attendance.dto.CreateClassRequest
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
    private val classSessionDao: ClassSessionDao,
    private val attendanceDao: AttendanceDao,
    private val pendingSessionDao: PendingSessionDao
) {
    private val _sessionStatus = MutableStateFlow(TeacherSessionController.SessionStatus())
    val sessionStatus = _sessionStatus.asStateFlow()

    val TAG="pendingSession"


    //
    fun updateStatus(newStatus: TeacherSessionController.SessionStatus) {
        _sessionStatus.value = newStatus
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

    fun getLocalClassSessionFlow(classId: String): Flow<List<ClassSessionEntity>> {
        return classSessionDao.getAllClassSessions(classId)
            .onStart {
                Log.i("classSession", "Started observing local  DB for class sessions ")
            }
            .catch { e ->
                Log.e("classSession", "Database read error: ${e.message}", e)
                // Emit an empty list to prevent the UI from crashing if the DB fails
                emit(emptyList())
            }
    }


    fun getLocalAttendanceFlow(classSessionId: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAllAttendances(classSessionId)
            .onStart {
                Log.i("classSession", "Started observing local  DB for class sessions ")
            }
            .catch { e ->
                Log.e("classSession", "Database read error: ${e.message}", e)
                // Emit an empty list to prevent the UI from crashing if the DB fails
                emit(emptyList())
            }
    }


   suspend fun createClass(className: String,section: String,duration: String){
       coroutineScope {
           try {
               Log.i("joinClass", "initiating the call to join class")

               val classReqDto= CreateClassRequest(
                   className=className,
                   section = section,
                   duration=duration
               )

               val classResponse = api.createClass(classReqDto)

               if (classResponse.isSuccessful && classResponse.body() != null) {

                   val classResponseBody = classResponse.body()!!
                   val classEntity = classResponseBody.toRoomEntity()

                   database.withTransaction {
                       classDao.insertClass(classEntity)
                   }
               }
           } catch (e: Exception) {
               e.printStackTrace()
               Log.e("joinClass", "${e.message}")
           }
       }
   }


    suspend fun syncAllClasses() {
        coroutineScope {
            try {
                Log.i("TeacherRepo", "Starting Eager Sync for Teacher")

                // 1. Fetch Class List from Spring Boot
                val classResponse = api.getAllClassesForTeacher()

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


    suspend fun syncAllClassSessions(classId: String) {
        coroutineScope {
            try {
                Log.i("classSession", "Starting Eager Sync for class-sessions")

                val classSessionResponse = api.getAllClassSessionForTeacher(classId)

                if (classSessionResponse.isSuccessful && classSessionResponse.body() != null) {
                    val classSessionDtos = classSessionResponse.body()!!

                    // 2. Fetch all attendance  concurrently
                    val attendanceJobs = classSessionDtos.map { classSessionDto ->
                        async {
                            Log.i(
                                "classSession",
                                "trying to fetch attendance for ${classSessionDto.classSessionId}"
                            )
                            val attendanceResponse =
                                api.getAllAttendancesForTeacher(classSessionDto.classSessionId)
                            if (attendanceResponse.isSuccessful) {
                                attendanceResponse.body()?.map { attendanceDto ->

                                    attendanceDto.toRoomEntity()
                                } ?: emptyList()
                            } else {
                                Log.e(
                                    "classSession",
                                    "Failed to fetch attendance for class session id: ${classSessionDto.classSessionId}"
                                )
                                emptyList()
                            }
                        }
                    }
                    // Wait for all attendance network calls to finish
                    val allAttendances = attendanceJobs.awaitAll().flatten()

                    val classSessionEntities = classSessionDtos.map { dto ->
                        dto.toRoomEntity()
                    }

                    //  Save everything atomically to Room to prevent data loss
                    database.withTransaction {
                        classSessionDao.clearAllClassSessions()
                        attendanceDao.clearAllAttendances()

                        classSessionDao.insertClassSession(classSessionEntities)
                        attendanceDao.insertAttendance(allAttendances)
                    }

                    Log.i(
                        "classSession",
                        "Successfully synced ${classSessionEntities.size} sessions and ${allAttendances.size} attendances."
                    )
                } else {
                    Log.e("classSession", "Server returned error: ${classSessionResponse.code()}")
                }
            } catch (e: Exception) {
                // If there's no internet, this catch block catches the Retrofit exception.
                //the UI just keeps displaying the old Room data!
                Log.e("classSession", "Network Sync Failed. Working offline. Error: ${e.message}")
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
                // Format: MMM d, yyyy • hh:mm a
                val todayDate = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault()).format(Date())

                // Query local Room DB
                val existingSession = pendingSessionDao.getSessionForToday(classId, todayDate)

                if (existingSession == null) {
                    //  First scan  Initialize map with 1 hit per student.
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

