package com.rohan.attendify_smart_attendance.dto

data class LoginRequest(
   private val email: String,
   private val password: String
)