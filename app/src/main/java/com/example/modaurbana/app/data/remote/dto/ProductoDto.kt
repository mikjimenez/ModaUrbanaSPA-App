package com.example.modaurbana.app.data.remote.dto

import com.example.modaurbana.app.models.Producto
import com.google.gson.annotations.SerializedName

data class ProductoDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val talla: String? = null,
    val material: String? = null,
    val estilo: String? = null,
    val descripcion: String? = null,
    val color: String? = null,
    val precio: Double,
    val stock: Int? = null,
    val categoria: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ProductoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Producto
)

data class ProductosResponse(
    val success: Boolean,
    val data: List<Producto>,
    val total: Int
)

data class CreateProductoRequest(
    val nombre: String,
    val talla: String? = null,
    val material: String? = null,
    val estilo: String? = null,
    val color: String? = null,
    val precio: Double,
    val stock: Int? = null,
    val categoria: String,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

data class UpdateProductoRequest(
    val nombre: String? = null,
    val talla: String? = null,
    val material: String? = null,
    val estilo: String? = null,
    val color: String? = null,
    val precio: Double? = null,
    val stock: Int? = null,
    val categoria: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

/**
 * Mapeo DTO -> dominio
 */
fun ProductoDto.toDomain(): Producto = Producto(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    talla = talla,
    material = material,
    estilo = estilo,
    precio = precio,
    imagen = imagen,
    imagenThumbnail = imagenThumbnail,
    categoria = categoria,
    stock = stock,
    createdAt = createdAt,
    updatedAt = updatedAt
)