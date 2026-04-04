package com.rohan.attendify_smart_attendance

import android.app.Application
import com.rohan.attendify_smart_attendance.api.RetrofitInstance
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository

class AttendifyApplication : Application() {

    // 'by lazy' means: "Don't build this until someone actually asks for it,
    // but once you build it, keep the same one forever."

    val database by lazy { AttendifyDatabase.getDatabase(this) }

    val api by lazy { RetrofitInstance.api }

    // Here is your SINGLE source of truth!
    val teacherRepository by lazy {
        TeacherSessionRepository(
            api = api,
            rosterDao = database.studentRosterDao(),
            pendingSessionDao = database.pendingSessionDao()
        )
    }
}