package com.rohan.attendify_smart_attendance.api

import com.rohan.attendify_smart_attendance.dto.AttendanceDto
import retrofit2.http.Query
import com.rohan.attendify_smart_attendance.dto.ClassDto
import com.rohan.attendify_smart_attendance.dto.ClassSessionDto
import com.rohan.attendify_smart_attendance.dto.CreateClassRequest
import com.rohan.attendify_smart_attendance.dto.LoginRequest
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.dto.SessionSyncRequest
import com.rohan.attendify_smart_attendance.dto.SignupRequest
import com.rohan.attendify_smart_attendance.dto.StudentRosterDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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
        @Path("classId") classId: String): Response<List<StudentRosterDto>>

    @POST("/api/teacher/session/sync")
    suspend fun uploadOfflineSessions(
        @Body syncRequest: List<SessionSyncRequest>): Response<Unit>



    @GET("/api/teacher/class/getClasses")
    suspend fun getAllClassesForTeacher(): Response<List<ClassDto>>


    @GET("/api/teacher/class/classSession")
    suspend fun getAllClassSessionForTeacher(
        @Query("classId") classId: String): Response<List<ClassSessionDto>>


    @GET("api/teacher/classSession/getAllAttendances")
    suspend fun getAllAttendancesForTeacher(
        @Query("classSessionId")
        classId: String): Response<List<AttendanceDto>>



    @GET("/api/student/class/getClasses")
    suspend fun getAllClassesForStudent(): Response<List<ClassDto>>


    @POST("/api/student/class/joinClass")
    suspend fun joinClass(
        @Query("classCode") classCode: String): Response<ClassDto>


    @POST("/api/teacher/class/createClass")
    suspend fun createClass(@Body classDto: CreateClassRequest): Response<ClassDto>

}