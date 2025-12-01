package com.example.modaurbana.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PedidoItem(
    val producto: String,
    val cantidad: Int,
    val precio: Double? = null
)

data class Pedido(
    @SerializedName("_id")
    val id: String,
    val cliente: String,
    val items: List<PedidoItem>,
    val total: Double,
    val estado: String? = null,
    val direccionEntrega: String? = null,
    val notasEntrega: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PedidoResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Pedido
)

data class PedidosResponse(
    val success: Boolean,
    val data: List<Pedido>,
    val total: Int
)

data class CreatePedidoRequest(
    val cliente: String? = null,
    val items: List<PedidoItemRequest>,
    val direccionEntrega: String? = null,
    val notasEntrega: String? = null
)

data class PedidoItemRequest(
    val producto: String,
    val cantidad: Int
)

data class UpdatePedidoRequest(
    val estado: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)
