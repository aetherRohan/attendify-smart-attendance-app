package com.rohan.attendify_smart_attendance

import android.app.Application
import com.rohan.attendify_smart_attendance.api.RetrofitInstance
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.repository.AuthRepository
import com.rohan.attendify_smart_attendance.repository.StudentSessionRepository
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import com.rohan.attendify_smart_attendance.security.TokenManager


class AttendifyApplication : Application() {

    val tokenManager by lazy { TokenManager(this) }
    val database by lazy { AttendifyDatabase.getDatabase(this) }
    val api by lazy { RetrofitInstance.getApi(tokenManager) }


    val teacherRepository by lazy {
        TeacherSessionRepository(
            api = api,
            database = database,
            rosterDao = database.studentRosterDao(),
            classDao = database.classDao(),
            pendingSessionDao = database.pendingSessionDao()
        )
    }

    val studentRepository by lazy {

        StudentSessionRepository(
            classDao = database.classDao()
        )

    }

    val authRepository by lazy {
        AuthRepository(
            api,tokenManager
        )
    }


}