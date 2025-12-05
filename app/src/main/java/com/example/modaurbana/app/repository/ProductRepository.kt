package com.example.modaurbana.app.repository

import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.ProductoDto
import com.example.modaurbana.app.models.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(
    private val session: SessionManager
) {

    private val apiService = RetrofitClient.apiService

    /**
     * Obtiene la lista de productos desde el backend.
     */
    suspend fun getProductos(): List<ProductoDto> = withContext(Dispatchers.IO) {
        session.getAuthToken() ?: error("No hay token guardado (usuario no logueado)")

        val response = apiService.getProductos()

        if (!response.isSuccessful) {
            throw IllegalStateException("Error HTTP al obtener productos: ${response.code()}")
        }

        val body = response.body()
            ?: throw IllegalStateException("Respuesta vacía al obtener productos")

        if (body.success != true) {
            throw IllegalStateException(body.message ?: "Error desconocido al obtener productos")
        }

        body.data
    }

    /**
     * Obtiene el detalle de un producto por su ID.
     */
    suspend fun getProductoById(id: String): Producto = withContext(Dispatchers.IO) {
        session.getAuthToken() ?: error("No hay token guardado (usuario no logueado)")

        val response = apiService.getProductoById(id)

        if (!response.isSuccessful) {
            throw IllegalStateException("Error HTTP al obtener producto: ${response.code()}")
        }

        val body = response.body()
            ?: throw IllegalStateException("Respuesta vacía al obtener producto")

        if (!body.success) {
            throw IllegalStateException(body.message ?: "Producto no encontrado")
        }

        body.data
    }
}
