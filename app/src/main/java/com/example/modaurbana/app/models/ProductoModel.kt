package com.example.modaurbana.app.models
data class Producto(
    val id: String?,               // ID Mongo
    val nombre: String,            // Nombre visible
    val descripcion: String?,      // Descripción
    val talla: String?,            // XS/S/M/L etc
    val material: String?,         // Algodón / Cuero / Mezclilla
    val estilo: String?,           // Streetwear / Casual
    val precio: Double?,           // Precio
    val imagen: String?,           // Imagen principal
    val imagenThumbnail: String?,  // Thumbnail
    val categoria: String?,        // Categoria
    val stock: Int?,               // Stock
    val createdAt: String?,        // Fecha de creación
    val updatedAt: String?         // Fecha de actualización
)
