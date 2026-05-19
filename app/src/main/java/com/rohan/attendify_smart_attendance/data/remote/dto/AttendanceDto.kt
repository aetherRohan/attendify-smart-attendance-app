package com.rohan.attendify_smart_attendance.data.remote.dto

import com.rohan.attendify_smart_attendance.data.local.entity.AttendanceEntity

data class AttendanceDto(
    val classSessionId: String,
    val classId: String,
    val date: String,
    val studentId: String,
    val isPresent: Boolean,
    val studentName: String,
    val rollNumber: String?,

){
    fun toRoomEntity(): AttendanceEntity{
       return AttendanceEntity(
           classId = this.classId,
           date = this.date,
           classSessionId=this.classSessionId,
           studentId = this.studentId,
           isPresent = this.isPresent,
           studentName = this.studentName,
           rollNumber = this.rollNumber?:"",
       )
    }
}
