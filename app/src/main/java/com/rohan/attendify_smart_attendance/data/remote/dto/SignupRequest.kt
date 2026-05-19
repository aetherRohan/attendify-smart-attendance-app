package com.rohan.attendify_smart_attendance.data.remote.dto

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
)