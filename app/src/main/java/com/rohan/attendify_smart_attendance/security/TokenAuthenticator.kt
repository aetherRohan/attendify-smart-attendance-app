package com.rohan.attendify_smart_attendance.security

import android.content.Context
import android.content.Intent
import android.util.Log
import com.rohan.attendify_smart_attendance.MainActivity
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

class TokenAuthenticator(
    private val context: Context,
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) > 3) {
            forceLogout()
            return null
        }
        val refreshToken = runBlocking { tokenManager.getRefreshToken() }

        if (refreshToken.isNullOrBlank()) {
            forceLogout()
            return null
        }
        val jsonBody = JSONObject().apply {
            put("refreshToken", refreshToken)
        }.toString()

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val refreshRequest = Request.Builder()
            .url("http://localhost:8083/api/auth/refreshToken")
            .post(requestBody)
            .build()

        try {
            val refreshResponse = OkHttpClient().newCall(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {

                refreshResponse.body?.string()?.let { responseBodyString ->

                    val jsonObject = JSONObject(responseBodyString)
                    val newAccessToken = jsonObject.getString("accessToken")
                    val newRefreshToken = jsonObject.getString("refreshToken")

                    runBlocking {
                        tokenManager.updateTokens(
                            accessToken = newAccessToken,
                            refreshToken = newRefreshToken
                        )
                    }
                    //  Retry the request with   new access token
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken").build()
                }
            } else {
                // the refresh token is expired .
                Log.e("TokenAuthenticator", "Refresh API failed with code: ${refreshResponse.code}")
                forceLogout()
                return null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Network error during refresh", e)
            return null
        }
        return null
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private fun forceLogout() {

        runBlocking {
            tokenManager.clearSession()
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}