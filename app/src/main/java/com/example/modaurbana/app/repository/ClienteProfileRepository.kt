package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.*

/**
 * Repositorio para manejar el perfil del cliente
 */
class ClienteProfileRepository(context: Context) {

    private val apiService = RetrofitClient.apiService

    /**
     * Obtiene el perfil del cliente autenticado
     */
    suspend fun getMyProfile(): Result<ClienteProfile> {
        return try {
            val response = apiService!!.getMyProfile()

            if (response.isSuccessful && response.body() != null) {
                // Retornar directamente el DTO
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener perfil"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualiza el perfil del cliente
     */
    suspend fun updateMyProfile(
        nombre: String? = null,
        telefono: String? = null,
        direccion: String? = null,
        tallas: String? = null,
        preferencias: List<String>? = null
    ): Result<ClienteProfile> {
        return try {
            val request = UpdateClienteProfileRequest(
                nombre = nombre,
                telefono = telefono,
                direccion = direccion,
                tallas = tallas,
                preferencias = preferencias
            )

            val response = apiService!!.updateMyProfile(request)

            if (response.isSuccessful && response.body() != null) {
                // Retornar directamente el DTO
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al actualizar perfil"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}