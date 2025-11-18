package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO = Data Transfer Object
 * Este objeto representa los datos que VIAJAN entre tu app y el servidor
 */
data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("ubicacion")
    val ubicacion: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("image")
    val image: String? = null  // URL de imagen de perfil (opcional)
)