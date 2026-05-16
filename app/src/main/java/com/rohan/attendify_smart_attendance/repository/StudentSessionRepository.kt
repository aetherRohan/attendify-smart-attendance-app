package com.rohan.attendify_smart_attendance.repository

import android.util.Log
import androidx.room.withTransaction
import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.data.local.dao.ClassDao
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.domain.session.StudentSessionController
import com.rohan.attendify_smart_attendance.security.TokenManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

class StudentSessionRepository(
   private val classDao: ClassDao,
    private val tokenManager: TokenManager,
    private val api: ApiService,
    private val database: AttendifyDatabase
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

    suspend fun getBleUuid(): String{
        return tokenManager.getBleUuId()?:""
    }


    suspend fun syncAllStudentClass() {
        coroutineScope {
            try {
                Log.i("StudentRepo", "Starting Eager Sync for Student")

                // Fetch Class List from server
                val classResponse = api.getAllClassesForStudent()

                if (classResponse.isSuccessful && classResponse.body() != null) {
                    val classDtos = classResponse.body()!!

                    val classEntities = classDtos.map { dto ->
                        dto.toRoomEntity()
                    }

                    //  Save everything atomically to Room to prevent data loss
                    database.withTransaction {
                        classDao.clearAllClasses()
                        classDao.insertClasses(classEntities)
                    }

                    Log.i("StudentRepo", "Successfully synced ${classEntities.size} classes")
                } else {
                    Log.e("StudentRepo", "Server returned error: ${classResponse.code()}")
                }
            } catch (e: Exception) {
                // If there's no internet, this catch block catches the Retrofit exception.
                //the UI just keeps displaying the old Room data!
                Log.e("TeacherRepo", "Network Sync Failed. Working offline. Error: ${e.message}")
            }
        }
    }


    suspend fun joinClass(classCode: String) {
        coroutineScope {
            try {
                Log.i("joinClass", "initiating the call to join class")

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
                Log.e("joinClass", "${e.message}")
            }
        }
    }



}