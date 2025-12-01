package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    val email: String,
    val role: String,
    val avatar: String? = null,
    val isActive: Boolean,
    val emailVerified: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ProfileResponse(
    val success: Boolean,
    val data: User
)

data class UsersResponse(
    val success: Boolean,
    val data: List<User>
)