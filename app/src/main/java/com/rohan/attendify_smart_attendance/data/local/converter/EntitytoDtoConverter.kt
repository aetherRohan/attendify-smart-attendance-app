package com.rohan.attendify_smart_attendance.data.local.converter

import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity
import com.rohan.attendify_smart_attendance.dto.SessionSyncRequest

fun toSessionSyncDtoReq(pendingSessionEntity: List<PendingSessionEntity>): List<SessionSyncRequest>{

    return pendingSessionEntity.map { session->
        SessionSyncRequest(
            classId = session.classId,
            sessionDate = session.sessionStartDate,
            totalWindowsCount = session.totalWindows,
            studentWindowCounts = session.studentHitsMap
        )
    }
}