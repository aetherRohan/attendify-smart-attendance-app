package com.rohan.attendify_smart_attendance.repository

import com.rohan.attendify_smart_attendance.api.ApiService
import com.rohan.attendify_smart_attendance.dto.LoginRequest
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.dto.SignupRequest
import com.rohan.attendify_smart_attendance.security.TokenManager
import retrofit2.Response


class AuthRepository(val api: ApiService,val tokenManager: TokenManager) {

    // for login
    suspend fun login(email: String, password: String): Response<LoginResponse> {
       val response= api.loginUser(
            LoginRequest(email = email, password = password)
        )
        if (response.isSuccessful) {
            response.body()?.let { tokenManager.saveAccessToken(it.accessToken) }
        }
        return response
    }
    // for student sign up
    suspend fun signupStudent(name: String, email: String, password: String) =
        api.registerStudent(
            SignupRequest(name = name, email = email, password = password)
        )

    //for teacher sign up
    suspend fun signupTeacher(name: String, email: String, password: String) =
        api.registerTeacher(
            SignupRequest(name = name, email = email, password = password)
        )

}