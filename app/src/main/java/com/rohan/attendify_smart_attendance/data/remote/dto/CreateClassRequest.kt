package com.rohan.attendify_smart_attendance.data.remote.dto

data class CreateClassRequest(
    val className: String,
    val section: String,
    val duration: String
)
