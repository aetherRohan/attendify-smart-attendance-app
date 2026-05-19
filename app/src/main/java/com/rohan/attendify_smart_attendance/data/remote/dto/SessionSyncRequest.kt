package com.rohan.attendify_smart_attendance.data.remote.dto

import java.util.Date
import java.util.UUID
data class SessionSyncRequest (
     val classId: String,
     val sessionDate: String,
     val totalWindowsCount: Int,
     val studentWindowCounts: Map<String, Int>
)