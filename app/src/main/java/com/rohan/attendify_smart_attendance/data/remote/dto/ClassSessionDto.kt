package com.rohan.attendify_smart_attendance.data.remote.dto

import com.rohan.attendify_smart_attendance.data.local.entity.ClassSessionEntity


data class ClassSessionDto(
    val classSessionId: String,
    val classId: String,
    val date: String,
) {
    fun toRoomEntity(): ClassSessionEntity {
        return ClassSessionEntity(
            classSessionId = this.classSessionId,
            date = this.date,
            classId = this.classId,
        )
    }
}