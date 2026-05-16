package com.rohan.attendify_smart_attendance.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey
    val classId: String,
    val className: String,
    val classCode: String,
    val section: String,
    val duration: String,
)