package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Categoria(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CategoriaResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Categoria
)

data class CategoriasResponse(
    val success: Boolean,
    val data: List<Categoria>,
    val total: Int
)

data class CreateCategoriaRequest(
    val nombre: String,
    val descripcion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

data class UpdateCategoriaRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)
