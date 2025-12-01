package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.AppDatabase
import com.example.modaurbana.app.data.local.entity.ProductEntity
import com.example.modaurbana.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repositorio híbrido para productos
 * Combina datos locales (Room) y remotos (API)
 */
class ProductRepository(context: Context) {

    private val productDao = AppDatabase.getDatabase(context).productDao()
    private val apiService = RetrofitClient.ApiService

    // ==================== OPERACIONES LOCALES ====================

    /**
     * Obtiene productos de la base de datos local
     */
    fun getAllProductsLocal(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    /**
     * Obtiene productos por categoría (local)
     */
    fun getProductsByCategoryLocal(category: String): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(category)
    }

    /**
     * Obtiene un producto por ID (local)
     */
    suspend fun getProductByIdLocal(id: Int): Result<ProductEntity> {
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

    // ==================== OPERACIONES REMOTAS ====================

    /**
     * Obtiene productos de la API y los sincroniza con la BD local
     */
    suspend fun fetchProductsFromApi(): Result<List<ProductEntity>> {
        return try {
            val response = apiService.getProductos()

            if (response.isSuccessful && response.body() != null) {
                val productosDto = response.body()!!.data

                // Convertir DTOs a entities
                val products = productosDto.map { dto ->
                    ProductEntity(
                        id = 0, // Se autogenera
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

                // Guardar en base de datos local
                products.forEach { productDao.insertProduct(it) }

                Result.success(products)
            } else {
                Result.failure(Exception("Error al obtener productos de la API"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene productos por categoría desde la API
     */
    suspend fun fetchProductsByCategoria(categoriaId: String): Result<List<ProductEntity>> {
        return try {
            val response = apiService.getProductosByCategoria(categoriaId)

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

    /**
     * Obtiene productos en tendencia
     */
    suspend fun getTrendingProducts(): Result<List<ProductEntity>> {
        return try {
            val response = apiService.getProductosTendencias()

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
                Result.failure(Exception("Error al obtener tendencias"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // ==================== ESTRATEGIA HÍBRIDA ====================

    /**
     * Obtiene productos con estrategia cache-first
     * Intenta obtener de local, si falla o está vacío, obtiene de API
     */
    suspend fun getProductsWithCacheFirst(): Result<List<ProductEntity>> {
        return try {
            // Primero intenta obtener de local
            val localProducts = productDao.getAllProducts().first()

            if (localProducts.isNotEmpty()) {
                // Si hay datos locales, los retorna
                Result.success(localProducts)
            } else {
                // Si no hay datos locales, obtiene de la API
                fetchProductsFromApi()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}