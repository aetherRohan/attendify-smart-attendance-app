package com.rohan.attendify_smart_attendance.api

import com.rohan.attendify_smart_attendance.security.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://localhost:8083/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    fun getApi(tokenManager: TokenManager): ApiService {

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Keep your existing logging!
            // --- THE NEW TOKEN INTERCEPTOR ---
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                // Grab the token from Jetpack DataStore/Tink vault
                val token = tokenManager.getAccessTokenSync()

                // If the token exists, glue it to the header
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }

            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}