package com.rohan.attendify_smart_attendance.dto
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity

data class StudentRosterDto(
    val studentId: String,
    val classId: String,
    val bleUuid: String,
    val name: String,
    val rollNumber: String
) {

    fun toRoomEntity(): StudentRosterEntity {
        return StudentRosterEntity(
            studentId = this.studentId,
            classId = this.classId,
            bleUuid = this.bleUuid,
            name = this.name,
            rollNumber = this.rollNumber
        )
    }
}
