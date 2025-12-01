package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Cliente(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ClienteResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Cliente
)

data class ClientesResponse(
    val success: Boolean,
    val data: List<Cliente>,
    val total: Int
)

data class CreateClienteRequest(
    val nombre: String,
    val descripcion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

data class UpdateClienteRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)