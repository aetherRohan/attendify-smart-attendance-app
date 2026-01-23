package com.rohan.attendify_smart_attendance.api

import com.rohan.attendify_smart_attendance.dto.LoginRequest
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.dto.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/signup/teacher")
    suspend fun registerTeacher(@Body request: SignupRequest): Response<LoginResponse>

    @POST("api/auth/signup/student")
    suspend fun registerStudent(@Body request: SignupRequest): Response<LoginResponse>

}