package com.rohan.attendify_smart_attendance.data.remote.dto
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity

data class ClassDto(
    val classId: String,
    val classCode: String,
    val className: String,
    val section: String,
    val duration: String
) {
    fun toRoomEntity(): ClassEntity {

        return ClassEntity(
            classId = this.classId,
            className = this.className,
            section = this.section,
            duration = this.duration,
            classCode = this.classCode,
        )
    }
}