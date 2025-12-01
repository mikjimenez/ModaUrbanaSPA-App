package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClienteProfile(
    @SerializedName("_id")
    val id: String,
    val user: String,
    val nombre: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val tallas: String? = null,
    val preferencias: List<String>? = null,
    val isActive: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ClienteProfileResponse(
    val success: Boolean,
    val data: ClienteProfile
)

data class ClienteProfilesResponse(
    val success: Boolean,
    val data: List<ClienteProfile>
)

data class UpdateClienteProfileRequest(
    val nombre: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val tallas: String? = null,
    val preferencias: List<String>? = null
)