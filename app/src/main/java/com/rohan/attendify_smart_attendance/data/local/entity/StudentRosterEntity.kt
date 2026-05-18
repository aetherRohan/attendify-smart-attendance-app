package com.rohan.attendify_smart_attendance.data.local.entity
import androidx.room.Entity

@Entity(
    tableName = "student_roster",
    primaryKeys = ["studentId", "classId"]
)
data class StudentRosterEntity(
    val studentId: String,
    val classId: String,
    val bleUuid: String,
    val name: String,
    val rollNumber: String
)