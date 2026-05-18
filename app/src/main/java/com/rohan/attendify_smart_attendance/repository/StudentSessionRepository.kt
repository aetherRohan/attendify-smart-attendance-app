package com.rohan.attendify_smart_attendance.repository

import android.util.Log
import androidx.room.withTransaction
import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.data.local.dao.AttendanceDao
import com.rohan.attendify_smart_attendance.data.local.dao.ClassDao
import com.rohan.attendify_smart_attendance.data.local.entity.AttendanceEntity
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.domain.session.StudentSessionController
import com.rohan.attendify_smart_attendance.security.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

class StudentSessionRepository(
    private val classDao: ClassDao,
    private val attendanceDao: AttendanceDao,
    private val tokenManager: TokenManager,
    private val api: ApiService,
    private val database: AttendifyDatabase
) {
    private var _sessionStatus = MutableStateFlow(StudentSessionController.StudentSessionState())
    var sessionStatus = _sessionStatus.asStateFlow()


    fun updateStatus(newStatus: StudentSessionController.StudentSessionState) {
        _sessionStatus.value = newStatus
    }

    fun getLocalClassesFlow(): Flow<List<ClassEntity>> {

        return classDao.getAllClassesFlow()
            .catch { e ->
                Log.e("StudentRepo", "Database read error: ${e.message}", e)
                emit(emptyList<ClassEntity>())
            }
    }

    fun getLocalAttendanceFlow(classId: String, studentId: String): Flow<List<AttendanceEntity>> {

        return attendanceDao.getAttendance(classId = classId, studentId = studentId)
            .catch { e ->
                Log.e("StudentRepo", "Database read error: ${e.message}", e)
                emit(emptyList<AttendanceEntity>())
            }
    }

    suspend fun getBleUuid(): String {
        return tokenManager.getBleUuId() ?: ""
    }


    suspend fun syncAllStudentClass() {
        coroutineScope {
            try {
                val classResponse = api.getAllClassesForStudent()

                if (classResponse.isSuccessful && classResponse.body() != null) {
                    val classDtos = classResponse.body()!!

                    val classEntities = classDtos.map { dto ->
                        dto.toRoomEntity()
                    }
                    database.withTransaction {
                        classDao.clearAllClasses()
                        classDao.insertClasses(classEntities)
                    }
                } else {
                    Log.e("StudentRepo", "Server returned error: ${classResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("StudentRepo", "Network Sync Failed. Working offline. Error: ${e.message}")
            }
        }
    }

    suspend fun syncAllAttendance(classId: String, studentId: String) {
        coroutineScope {
            try {
                Log.i("StudentRepo", "Starting Eager Sync for session attendance")

                val attendanceResponse = api.getAllClassAttendanceForStudent(
                    classId = classId, studentId = studentId
                )

                if (attendanceResponse.isSuccessful && attendanceResponse.body() != null) {

                    val attendanceDtos = attendanceResponse.body()!!

                    val attendanceEntities = attendanceDtos.map { dto ->
                        dto.toRoomEntity()
                    }

                    database.withTransaction {
                        attendanceDao.clearAllAttendances()
                        attendanceDao.insertAttendance(attendanceEntities)
                    }
                } else {
                    Log.e("StudentRepo", "Server returned error: ${attendanceResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("StudentRepo", "Network Sync Failed. Working offline. Error: ${e.message}")
            }
        }
    }

    suspend fun joinClass(classCode: String) {
        coroutineScope {
            try {
                val classResponse = api.joinClass(classCode)

                if (classResponse.isSuccessful && classResponse.body() != null) {

                    val classResponseBody = classResponse.body()!!
                    val classEntity = classResponseBody.toRoomEntity()

                    database.withTransaction {
                        classDao.insertClass(classEntity)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("StudentRepo", "${e.message}")
            }
        }
    }
}