package com.rohan.attendify_smart_attendance.dto

data class CreateClassRequest(
    val className: String,
    val section: String,
    val duration: String
)
