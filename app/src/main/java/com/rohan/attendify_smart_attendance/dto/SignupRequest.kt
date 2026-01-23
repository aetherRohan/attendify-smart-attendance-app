package com.rohan.attendify_smart_attendance.dto

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
)