package com.example.modaurbana.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String, // "Polera", "Pantalón", "Zapatillas", "Hoodie"
    val size: String, // "S", "M", "L", "XL"
    val material: String, // "Algodón Reciclado", "Poliéster Orgánico"
    val price: Double,
    val stock: Int,
    val imageUrl: String? = null,
    val description: String
)