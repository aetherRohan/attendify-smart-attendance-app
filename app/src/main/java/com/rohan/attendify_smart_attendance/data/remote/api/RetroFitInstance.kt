package com.rohan.attendify_smart_attendance.data.remote.api

import android.content.Context
import com.rohan.attendify_smart_attendance.security.TokenAuthenticator
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


    fun getApi(context: Context, tokenManager: TokenManager): ApiService {

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->

                val requestBuilder = chain.request().newBuilder()

                // Grab  token from  DataStore
                val token = tokenManager.getAccessTokenSync()

                // If  token exists, add it to the header
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }

            .authenticator(TokenAuthenticator(context = context, tokenManager = tokenManager))
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