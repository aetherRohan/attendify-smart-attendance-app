package com.rohan.attendify_smart_attendance.dto

data class LoginResponse(
    val message: String,
    val role: String,
    val userId: Long,
    val fullName: String,
    val token: String
)