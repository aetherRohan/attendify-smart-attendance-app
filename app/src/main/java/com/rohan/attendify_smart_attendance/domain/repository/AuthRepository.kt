package com.rohan.attendify_smart_attendance.domain.repository

import com.rohan.attendify_smart_attendance.data.remote.api.ApiService
import com.rohan.attendify_smart_attendance.data.local.AttendifyDatabase
import com.rohan.attendify_smart_attendance.data.remote.dto.LoginRequest
import com.rohan.attendify_smart_attendance.data.remote.dto.LoginResponse
import com.rohan.attendify_smart_attendance.data.remote.dto.SignupRequest
import com.rohan.attendify_smart_attendance.security.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response


class AuthRepository(
    val api: ApiService,
    val tokenManager: TokenManager,
    val database: AttendifyDatabase
) {

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val response = api.loginUser(
            LoginRequest(email = email, password = password)
        )
        if (response.isSuccessful) {
            response.body()?.let {
                tokenManager.saveAccessToken(it.accessToken)
                tokenManager.saveUserDetails(
                    it.name,
                    it.role,
                    it.userId,
                    it.bleUuid,
                    it.refreshToken
                )
            }
        }
        return response
    }


    suspend fun signupStudent(name: String, email: String, password: String) =
        api.registerStudent(
            SignupRequest(name = name, email = email, password = password)
        )


    suspend fun signupTeacher(name: String, email: String, password: String) =
        api.registerTeacher(
            SignupRequest(name = name, email = email, password = password)
        )

    suspend fun performLogout() {

        withContext(Dispatchers.IO) {
            // 1. Wipe the Tokens and UUID
            tokenManager.clearSession()

            database.clearAllTables()
        }
    }
}