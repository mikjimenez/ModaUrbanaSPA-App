package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.*

/**
 * Repositorio para manejar el carrito desde la API
 * Trabaja únicamente con DTOs por el mismo problema de antes kjvbsdn
 */
class CartRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService
    private val sessionManager = SessionManager(context)

    /**
     * Obtiene todos los carritos (ADMIN)
     */
    suspend fun getAllCarritos(): Result<List<CarritoItem>> {
        return try {
            val response = apiService!!.getCarritos()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener carritos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene un carrito por ID
     */
    suspend fun getCarritoById(id: String): Result<Carrito> {
        return try {
            val response = apiService!!.getCarritoById(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Carrito no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene el carrito del cliente autenticado
     */
    suspend fun getMyCarrito(): Result<Carrito> {
        return try {
            val clienteId = sessionManager.getUserId()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val response = apiService!!.getCarritoByCliente(clienteId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                // Si no existe carrito, crear uno nuevo
                if (response.code() == 404) {
                    createCarrito()
                } else {
                    Result.failure(Exception("Error al obtener carrito"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crea un nuevo carrito para el cliente
     */
    private suspend fun createCarrito(): Result<Carrito> {
        return try {
            val clienteId = sessionManager.getUserId()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val request = CreateCarritoRequest(
                nombre = "Mi Carrito",
                descripcion = "Carrito de compras"
            )

            val response = apiService!!.createCarrito(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al crear carrito"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Agrega un item al carrito
     */
    suspend fun agregarItemCarrito(
        productoId: String,
        talla: String?,
        cantidad: Int
    ): Result<Carrito> {
        return try {
            val clienteId = sessionManager.getUserId()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val request = AgregarItemCarritoRequest(
                clienteId = clienteId,
                productoId = productoId,
                talla = talla,
                cantidad = cantidad
            )

            val response = apiService!!.agregarItemCarrito(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al agregar al carrito"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Remueve un item del carrito
     */
    suspend fun removerItemCarrito(itemId: String): Result<String> {
        return try {
            val clienteId = sessionManager.getUserId()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val response = apiService!!.removerItemCarrito(clienteId, itemId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Error al remover item"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualiza un carrito
     */
    suspend fun updateCarrito(
        carritoId: String,
        nombre: String? = null,
        descripcion: String? = null
    ): Result<Carrito> {
        return try {
            val request = UpdateCarritoRequest(
                nombre = nombre,
                descripcion = descripcion
            )

            val response = apiService!!.updateCarrito(carritoId, request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al actualizar carrito"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Elimina un carrito
     */
    suspend fun deleteCarrito(carritoId: String): Result<String> {
        return try {
            val response = apiService!!.deleteCarrito(carritoId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Error al eliminar carrito"))
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
    ): Result<Pedido> {
        return try {
            val clienteId = sessionManager.getUserId()
                ?: return Result.failure(Exception("No hay sesión activa"))

            val request = ConfirmarPedidoRequest(
                clienteId = clienteId,
                direccionEntrega = direccionEntrega,
                metodoPago = metodoPago
            )

            val response = apiService!!.confirmarPedido(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al confirmar pedido"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Calcula el total del carrito actual
     */
    suspend fun calcularTotalCarrito(): Result<Double> {
        return try {
            val carritoResult = getMyCarrito()

            carritoResult.fold(
                onSuccess = { carrito ->
                    val total = carrito.total ?: 0.0
                    Result.success(total)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al calcular total: ${e.message}"))
        }
    }

    /**
     * Obtiene la cantidad de items en el carrito
     */
    suspend fun getCartItemCount(): Result<Int> {
        return try {
            val carritoResult = getMyCarrito()

            carritoResult.fold(
                onSuccess = { carrito ->
                    val count = carrito.items?.size ?: 0
                    Result.success(count)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener cantidad: ${e.message}"))
        }
    }
}