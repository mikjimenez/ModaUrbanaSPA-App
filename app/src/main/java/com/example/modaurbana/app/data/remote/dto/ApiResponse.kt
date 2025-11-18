package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? =null,

    @SerializedName("data")
    val data: T? =null,

    @SerializedName("total")
    val total: String? =null,
)