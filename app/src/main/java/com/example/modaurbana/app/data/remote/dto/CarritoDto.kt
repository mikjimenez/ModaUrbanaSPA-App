package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CarritoItem(
    val producto: Producto,
    val cantidad: Int,
    val talla: String? = null
)

data class Carrito(
    @SerializedName("_id")
    val id: String,
    val cliente: String,
    val items: List<CarritoItem>? = null,
    val total: Double? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CarritoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Carrito
)

data class CarritosResponse(
    val success: Boolean,
    val data: List<Carrito>,
    val total: Int
)

data class CreateCarritoRequest(
    val nombre: String,
    val descripcion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

data class UpdateCarritoRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

data class AgregarItemCarritoRequest(
    val clienteId: String,
    val productoId: String,
    val talla: String? = null,
    val cantidad: Int
)

data class ConfirmarPedidoRequest(
    val clienteId: String,
    val direccionEntrega: String,
    val metodoPago: String
)