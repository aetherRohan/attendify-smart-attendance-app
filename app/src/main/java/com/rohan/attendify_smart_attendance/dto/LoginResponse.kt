package com.rohan.attendify_smart_attendance.dto

data class LoginResponse(
    val message: String,
    val role: String,
    val userId: String,
    val name: String,
    val accessToken: String,
    val refreshToken: String
)