package com.example.modaurbana.app.data.remote.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val total: Int? = null
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

data class HealthCheckResponse(
    val status: String,
    val timestamp: String
)