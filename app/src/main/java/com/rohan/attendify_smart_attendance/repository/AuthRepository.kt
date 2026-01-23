package com.rohan.attendify_smart_attendance.repository

import com.rohan.attendify_smart_attendance.api.RetrofitInstance
import com.rohan.attendify_smart_attendance.dto.LoginRequest
import com.rohan.attendify_smart_attendance.dto.SignupRequest

class AuthRepository(val retrofit: RetrofitInstance) {

    // for login
    suspend fun login(email: String, password: String) =
        retrofit.api.loginUser(
            LoginRequest(email = email, password = password)
        )

    // for student sign up
    suspend fun signupStudent(name: String, email: String, password: String) =
        retrofit.api.registerStudent(
            SignupRequest(name = name, email = email, password = password)
        )

    //for teacher sign up
    suspend fun signupTeacher(name: String, email: String, password: String) =
        retrofit.api.registerTeacher(
            SignupRequest(name = name, email = email, password = password)
        )

}