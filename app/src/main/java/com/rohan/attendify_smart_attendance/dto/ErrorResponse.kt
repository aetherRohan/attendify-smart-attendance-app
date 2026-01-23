package com.rohan.attendify_smart_attendance.dto

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message")
    val message: String
)