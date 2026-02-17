package com.rohan.attendify_smart_attendance.repository

import com.rohan.attendify_smart_attendance.domain.session.TeacherSessionController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AttendanceRepository {

    private val _sessionStatus = MutableStateFlow(TeacherSessionController.SessionStatus())
    val sessionStatus = _sessionStatus.asStateFlow()
    fun updateStatus(newStatus: TeacherSessionController.SessionStatus) {
        _sessionStatus.value = newStatus
    }
}