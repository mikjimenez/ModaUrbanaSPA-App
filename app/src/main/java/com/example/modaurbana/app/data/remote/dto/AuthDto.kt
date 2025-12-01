package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "CLIENTE",
    val nombre: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val tallas: String? = null,
    val preferencias: List<String>? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData
)

data class AuthData(
    val user: User,
    @SerializedName("access_token")
    val accessToken: String
)