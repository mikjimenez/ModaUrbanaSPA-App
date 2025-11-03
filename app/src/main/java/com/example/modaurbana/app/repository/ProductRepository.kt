package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.AppDatabase
import com.example.modaurbana.app.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(context: Context) {
    private val productDao = AppDatabase.getDatabase(context).productDao()

    /**
     * Obtiene todos los productos
     */
    fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    /**
     * Obtiene productos por categoría
     */
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(category)
    }

    /**
     * Obtiene un producto por ID
     */
    suspend fun getProductById(id: Int): Result<ProductEntity> {
        return try {
            val product = productDao.getProductById(id)
            if (product != null) {
                Result.success(product)
            } else {
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}