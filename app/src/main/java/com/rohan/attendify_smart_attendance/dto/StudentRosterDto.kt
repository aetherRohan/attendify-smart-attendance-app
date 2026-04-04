package com.rohan.attendify_smart_attendance.dto

import com.google.gson.annotations.SerializedName
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity

data class StudentRosterDto(
    @SerializedName("studentId") val studentId: Long,
    @SerializedName("classId") val classId: String,
    @SerializedName("bleUuid") val bleUuid: String,
    @SerializedName("name") val name: String,
    @SerializedName("rollNumber") val rollNumber: String
) {
    // A handy function to instantly convert this Network object into a Room object
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