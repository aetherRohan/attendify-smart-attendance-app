package com.rohan.attendify_smart_attendance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_sessions")
data class ClassSessionEntity(
    @PrimaryKey
    val classSessionId: String,
    val classId: String,
    val date: String
)
