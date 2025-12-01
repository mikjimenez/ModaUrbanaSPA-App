package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.AppDatabase
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.local.entity.CartItemEntity
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.*
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio híbrido para el carrito
 * Maneja carrito local y sincronización con API
 */
class CartRepositoryHybrid(private val context: Context) {

    private val cartDao = AppDatabase.getDatabase(context).cartDao()
    private val apiService = RetrofitClient.ApiService
    private val sessionManager = SessionManager(context)

    // ==================== OPERACIONES LOCALES ====================

    /**
     * Obtiene items del carrito local
     */
    fun getAllCartItems(): Flow<List<CartItemEntity>> {
        return cartDao.getAllCartItems()
    }

    /**
     * Obtiene el precio total local
     */
    fun getTotalPrice(): Flow<Double?> {
        return cartDao.getTotalPrice()
    }

    /**
     * Agrega un producto al carrito local
     */
    suspend fun addToCart(
        productId: Int,
        productName: String,
        size: String,
        price: Double,
        imageUrl: String?
    ): Result<Unit> {
        return try {
            val cartItem = CartItemEntity(
                productId = productId,
                productName = productName,
                size = size,
                price = price,
                quantity = 1,
                imageUrl = imageUrl
            )
            cartDao.addToCart(cartItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la cantidad local
     */
    suspend fun updateQuantity(item: CartItemEntity, newQuantity: Int): Result<Unit> {
        return try {
            if (newQuantity <= 0) {
                cartDao.removeFromCart(item)
            } else {
                val updatedItem = item.copy(quantity = newQuantity)
                cartDao.updateCartItem(updatedItem)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un item del carrito local
     */
    suspend fun removeFromCart(item: CartItemEntity): Result<Unit> {
        return try {
            cartDao.removeFromCart(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Limpia el carrito local
     */
    suspend fun clearCart(): Result<Unit> {
        return try {
            cartDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== OPERACIONES REMOTAS ====================

    /**
     * Agrega un item al carrito en la API
     */
    suspend fun addItemToApiCart(
        productoId: String,
        talla: String?,
        cantidad: Int
    ): Result<Unit> {
        return try {
            val clienteId = sessionManager.getUserId() ?: return Result.failure(
                Exception("No hay sesión activa")
            )

            val request = AgregarItemCarritoRequest(
                clienteId = clienteId,
                productoId = productoId,
                talla = talla,
                cantidad = cantidad
            )

            val response = apiService.agregarItemCarrito(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al agregar al carrito"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene el carrito del cliente desde la API
     */
    suspend fun getCartFromApi(): Result<Carrito> {
        return try {
            val clienteId = sessionManager.getUserId() ?: return Result.failure(
                Exception("No hay sesión activa")
            )

            val response = apiService.getCarritoByCliente(clienteId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener carrito"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Confirma el pedido (convierte carrito en pedido)
     */
    suspend fun confirmarPedido(
        direccionEntrega: String,
        metodoPago: String
    ): Result<String> {
        return try {
            val clienteId = sessionManager.getUserId() ?: return Result.failure(
                Exception("No hay sesión activa")
            )

            val request = ConfirmarPedidoRequest(
                clienteId = clienteId,
                direccionEntrega = direccionEntrega,
                metodoPago = metodoPago
            )

            val response = apiService.confirmarPedido(request)

            if (response.isSuccessful && response.body() != null) {
                val pedidoId = response.body()!!.data.id

                // Limpiar carrito local después de confirmar
                clearCart()

                Result.success(pedidoId)
            } else {
                Result.failure(Exception("Error al confirmar pedido"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Sincroniza el carrito local con la API
     * (Útil para cuando el usuario inicia sesión)
     */
    suspend fun syncCartWithApi(): Result<Unit> {
        return try {
            // Obtener items locales
            val localItems = cartDao.getAllCartItems()

            // Por cada item local, agregarlo a la API
            // (Esto es una implementación simple, podrías necesitar lógica más compleja)
            localItems.collect { items ->
                items.forEach { item ->
                    // Necesitarías el ID del producto en la API
                    // Este es un ejemplo simplificado
                    addItemToApiCart(
                        productoId = item.productId.toString(),
                        talla = item.size,
                        cantidad = item.quantity
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}