package com.rohan.attendify_smart_attendance.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "attendance",
    primaryKeys = ["classSessionId","studentId"]
)
data class AttendanceEntity(
    val classId: String,
    val classSessionId: String,
    val studentId: String,
    val date: String,
    val isPresent: Boolean,
    val studentName: String,
    val rollNumber: String,
)
