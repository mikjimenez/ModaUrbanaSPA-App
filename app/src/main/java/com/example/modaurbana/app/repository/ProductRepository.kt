package com.example.modaurbana.app.repository

import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.local.entity.ProductEntity
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.ProductoDto
import com.example.modaurbana.app.data.remote.dto.toDomain
import com.example.modaurbana.app.models.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio híbrido para productos
 * Combina datos locales (Room) y remotos (API)
 */
class ProductRepository(private val session : SessionManager) {

    private val apiService = RetrofitClient.ApiService

    suspend fun getProductos(): List<Producto> = withContext(Dispatchers.IO) {
        val token = session.getAuthToken() ?: error ("Error: No hay usuarios logeado con este token")

        val resp = apiService.getProductos()

        if (!resp.success) {
            throw IllegalStateException(resp.message() ?: "Error desconocido al obtener productos")
        }

        val dtoList: List<Producto> = resp.data ?: emptyList()
        dtoList.map { it.toDomain() }
    }

    /**
     * Obtiene productos por categoría desde la API
     */
    suspend fun fetchProductsByCategoria(categoriaId: String): Result<List<ProductEntity>> {
        return try {
            val response = apiService.getProductosByCategoria(categoriaId)

            if (response.isSuccessful && response.body() != null) {
                val ProductEntity = response.body()!!.data

                val products = ProductEntity.map { dto ->
                    ProductEntity(
                        id = 0,
                        name = dto.nombre,
                        category = dto.categoria?. ?: "Sin categoría",
                        size = dto.talla ?: "Única",
                        material = dto.material ?: "No especificado",
                        price = dto.precio,
                        stock = dto.stock ?: 0,
                        imageUrl = dto.imagen,
                        description = "${dto.estilo ?: ""} ${dto.color ?: ""}".trim()
                    )
                }

                Result.success(products)
            } else {
                Result.failure(Exception("Error al obtener productos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Filtra productos con parámetros
     */
    suspend fun filtrarProductos(
        talla: String? = null,
        material: String? = null,
        estilo: String? = null,
        genero: String? = null,
        precioMin: Double? = null,
        precioMax: Double? = null
    ): Result<List<ProductEntity>> {
        return try {
            val response = apiService.filtrarProductos(
                talla = talla,
                material = material,
                estilo = estilo,
                genero = genero,
                precioMin = precioMin,
                precioMax = precioMax
            )

            if (response.isSuccessful && response.body() != null) {
                val productosDto = response.body()!!.data

                val products = productosDto.map { dto ->
                    ProductEntity(
                        id = 0,
                        name = dto.nombre,
                        category = dto.categoria?.nombre ?: "Sin categoría",
                        size = dto.talla ?: "Única",
                        material = dto.material ?: "No especificado",
                        price = dto.precio,
                        stock = dto.stock ?: 0,
                        imageUrl = dto.imagen,
                        description = "${dto.estilo ?: ""} ${dto.color ?: ""}".trim()
                    )
                }

                Result.success(products)
            } else {
                Result.failure(Exception("Error al filtrar productos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}