package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.*

/**
 * Repositorio para manejar pedidos
 */
class PedidoRepository(private val context: Context) {

    private val apiService = RetrofitClient.ApiService
    private val sessionManager = SessionManager(context)

    /**
     * Obtiene todos los pedidos (ADMIN)
     */
    suspend fun getAllPedidos(): Result<List<Pedido>> {
        return try {
            val response = apiService!!.getPedidos()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener pedidos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene un pedido por ID
     */
    suspend fun getPedidoById(id: String): Result<Pedido> {
        return try {
            val response = apiService!!.getPedidoById(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Pedido no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crea un nuevo pedido
     */
    suspend fun createPedido(
        items: List<PedidoItemRequest>,
        direccionEntrega: String? = null,
        notasEntrega: String? = null
    ): Result<Pedido> {
        return try {
            val clienteId = sessionManager.getUserId()

            val request = CreatePedidoRequest(
                cliente = clienteId,
                items = items,
                direccionEntrega = direccionEntrega,
                notasEntrega = notasEntrega
            )

            val response = apiService!!.createPedido(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al crear pedido"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualiza el estado de un pedido
     */
    suspend fun updatePedidoEstado(
        pedidoId: String,
        estado: String
    ): Result<Pedido> {
        return try {
            val request = UpdatePedidoRequest(estado = estado)
            val response = apiService!!.updatePedido(pedidoId, request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al actualizar pedido"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Elimina un pedido
     */
    suspend fun deletePedido(id: String): Result<String> {
        return try {
            val response = apiService!!.deletePedido(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Error al eliminar pedido"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}