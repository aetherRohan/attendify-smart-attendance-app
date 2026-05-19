package com.rohan.attendify_smart_attendance

import android.app.Application
import com.rohan.attendify_smart_attendance.data.remote.api.RetrofitInstance
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.domain.repository.AuthRepository
import com.rohan.attendify_smart_attendance.domain.repository.StudentSessionRepository
import com.rohan.attendify_smart_attendance.domain.repository.TeacherSessionRepository
import com.rohan.attendify_smart_attendance.security.TokenManager


class AttendifyApplication : Application() {

    val tokenManager by lazy { TokenManager(this) }
    val database by lazy { AttendifyDatabase.getDatabase(this) }
    val api by lazy { RetrofitInstance.getApi(this,tokenManager) }


    val teacherRepository by lazy {
        TeacherSessionRepository(
            api = api,
            database = database,
            rosterDao = database.studentRosterDao(),
            classDao = database.classDao(),
            classSessionDao = database.classSessionDao(),
            attendanceDao = database.attendanceDao(),
            pendingSessionDao = database.pendingSessionDao()
        )
    }

    val studentRepository by lazy {

        StudentSessionRepository(
            classDao = database.classDao(),
            attendanceDao = database.attendanceDao(),
            tokenManager = tokenManager,
            api = api,
            database = database
        )

    }

    val authRepository by lazy {
        AuthRepository(
            api,tokenManager,database
        )
    }


}