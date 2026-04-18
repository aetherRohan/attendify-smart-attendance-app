package com.rohan.attendify_smart_attendance.api

import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity
import com.rohan.attendify_smart_attendance.dto.LoginRequest
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.dto.SessionSyncRequest
import com.rohan.attendify_smart_attendance.dto.SignupRequest
import com.rohan.attendify_smart_attendance.dto.StudentRosterDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/signup/teacher")
    suspend fun registerTeacher(@Body request: SignupRequest): Response<LoginResponse>

    @POST("api/auth/signup/student")
    suspend fun registerStudent(@Body request: SignupRequest): Response<LoginResponse>

    @GET("/api/teacher/class/{classId}/students")
    suspend fun getClassRoster(
        @Path("classId") classId: String
    ): Response<List<StudentRosterDto>>


    @POST("/api/teacher/session/sync")
    suspend fun uploadOfflineSessions(
        @Body syncRequest: List<SessionSyncRequest>
    ): Response<Unit>

}