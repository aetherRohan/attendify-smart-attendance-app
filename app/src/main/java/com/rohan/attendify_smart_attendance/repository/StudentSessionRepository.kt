package com.rohan.attendify_smart_attendance.repository

import com.rohan.attendify_smart_attendance.domain.session.StudentSessionController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object StudentSessionRepository {

    private var _sessionStatus = MutableStateFlow(StudentSessionController.StudentSessionState())
    var sessionStatus = _sessionStatus.asStateFlow()
    fun updateStatus(newStatus: StudentSessionController.StudentSessionState) {
        _sessionStatus.value = newStatus
    }
}