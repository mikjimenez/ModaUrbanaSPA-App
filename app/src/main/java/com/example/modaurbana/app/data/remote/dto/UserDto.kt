package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,

    val email: String,
    val role: String,
    val nombre: String,
    val avatar: String? = null,
    val telefono: String,
    val isActive: Boolean?,
    val direccion: String?,
    val emailVerified: Boolean?,
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