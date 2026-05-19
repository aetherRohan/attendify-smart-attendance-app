package com.rohan.attendify_smart_attendance.domain.repository


import android.util.Log
import androidx.room.withTransaction
import com.rohan.attendify_smart_attendance.data.remote.api.ApiService
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
import com.rohan.attendify_smart_attendance.data.remote.dto.CreateClassRequest
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

    fun updateStatus(newStatus: TeacherSessionController.SessionStatus) {
        _sessionStatus.value = newStatus
    }

    suspend fun getStudentsForClass(classId: String): List<StudentRosterEntity> {
        return try {
            rosterDao.getStudentsForClass(classId)
        } catch (e: Exception) {
            Log.e("TeacherRepo", "CRASH IN DB READ: ${e.message}", e)
            emptyList()
        }
    }

    fun getLocalClassesFlow(): Flow<List<ClassEntity>> {
        return classDao.getAllClassesFlow()
            .catch { e ->
                Log.e("TeacherRepo", "Database read error: ${e.message}", e)
                emit(emptyList<ClassEntity>())
            }
    }

    fun getLocalClassSessionFlow(classId: String): Flow<List<ClassSessionEntity>> {
        return classSessionDao.getAllClassSessions(classId)
            .catch { e ->
                Log.e("TeacherRepo", "Database read error: ${e.message}", e)
                emit(emptyList<ClassSessionEntity>())
            }
    }


    fun getLocalAttendanceFlow(classSessionId: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAllAttendances(classSessionId)
            .onStart {
                Log.i("TeacherRepo", "Started observing local  DB for class sessions ")
            }
            .catch { e ->
                Log.e("TeacherRepo", "Database read error: ${e.message}", e)
                emit(emptyList())
            }
    }


    suspend fun createClass(className: String, section: String, duration: String) {
        coroutineScope {
            try {
                val classReqDto = CreateClassRequest(
                    className = className,
                    section = section,
                    duration = duration
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
                Log.e("TeacherRepo", "${e.message}")
            }
        }
    }


    suspend fun syncAllClasses() {
        coroutineScope {
            try {
                val classResponse = api.getAllClassesForTeacher()

                if (classResponse.isSuccessful && classResponse.body() != null) {
                    val classDtos = classResponse.body()!!
                    val rosterJobs = classDtos.map { classDto ->
                        async {
                            val rosterResponse = api.getClassRoster(classDto.classId)
                            if (rosterResponse.isSuccessful) {
                                rosterResponse.body()?.map { studentDto ->

                                    studentDto.toRoomEntity()
                                } ?: emptyList()
                            } else {
                                Log.e(
                                    "TeacherRepo",
                                    "Failed to fetch roster for ${classDto.classId}"
                                )
                                emptyList()
                            }
                        }
                    }
                    val allStudents = rosterJobs.awaitAll().flatten()

                    val classEntities = classDtos.map { dto ->
                        dto.toRoomEntity()
                    }
                    database.withTransaction {
                        classDao.clearAllClasses()
                        rosterDao.clearAllStudents()

                        classDao.insertClasses(classEntities)
                        rosterDao.insertRoster(allStudents)
                    }

                } else {
                    Log.e("TeacherRepo", "Server returned error: ${classResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("TeacherRepo", "Network Sync Failed. Working offline. Error: ${e.message}")
            }
        }
    }


    suspend fun syncAllClassSessions(classId: String) {
        coroutineScope {
            try {
                val classSessionResponse = api.getAllClassSessionForTeacher(classId)

                if (classSessionResponse.isSuccessful && classSessionResponse.body() != null) {
                    val classSessionDtos = classSessionResponse.body()!!

                    val attendanceJobs = classSessionDtos.map { classSessionDto ->
                        async {
                            val attendanceResponse =
                                api.getAllAttendancesForTeacher(classSessionDto.classSessionId)
                            if (attendanceResponse.isSuccessful) {
                                attendanceResponse.body()?.map { attendanceDto ->

                                    attendanceDto.toRoomEntity()
                                } ?: emptyList()
                            } else {
                                emptyList()
                            }
                        }
                    }
                    val allAttendances = attendanceJobs.awaitAll().flatten()

                    val classSessionEntities = classSessionDtos.map { dto ->
                        dto.toRoomEntity()
                    }
                    database.withTransaction {
                        classSessionDao.clearAllClassSessions()
                        attendanceDao.clearAllAttendances()

                        classSessionDao.insertClassSession(classSessionEntities)
                        attendanceDao.insertAttendance(allAttendances)
                    }
                } else {
                    Log.e("TeacherRepo", "Server returned error: ${classSessionResponse.code()}")
                }
            } catch (e: Exception) {
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

                val todayDate =
                    SimpleDateFormat("MMM d, yyyy • hh:00 a", Locale.getDefault()).format(Date())

                val existingSession = pendingSessionDao.getSessionForToday(classId, todayDate)

                if (existingSession == null) {
                    val initialHits = scannedStudents.associateWith { 1 }

                    val newSession = PendingSessionEntity(
                        classId = classId,
                        sessionStartDate = todayDate,
                        totalWindows = windowIndex,
                        studentHitsMap = initialHits
                    )
                    pendingSessionDao.insertPendingSession(newSession)
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
                }
            } catch (e: Exception) {
                Log.e("TeacherRepo", "Failed to record window attendance safely: ${e.message}", e)
            }
        }
    }
}

