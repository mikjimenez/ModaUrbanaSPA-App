package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.*
import com.example.modaurbana.app.data.local.entity.UserEntity

/**
 * Repositorio para manejar autenticación con la API
 */
class AuthRepository(private val context: Context) {

    private val apiService = RetrofitClient.ApiService
    private val sessionManager = SessionManager(context)

    /**
     * Registra un nuevo usuario en la API
     */
    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        telefono: String? = null,
        direccion: String? = null,
        tallas: String? = null,
        preferencias: List<String>? = null
    ): Result<AuthData> {
        return try {
            val request = RegisterRequest(
                email = email,
                password = password,
                role = "CLIENTE",
                nombre = nombre,
                telefono = telefono,
                direccion = direccion,
                tallas = tallas,
                preferencias = preferencias
            )

            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar token y datos de usuario
                sessionManager.saveAuthToken(authResponse.data.accessToken)
                sessionManager.saveUserData(
                    userId = authResponse.data.user.id,
                    email = authResponse.data.user.email,
                    role = authResponse.data.user.role,
                    name = nombre
                )

                // Retornar los datos completos de autenticación
                Result.success(authResponse.data)
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "El email ya está registrado"
                    400 -> "Datos de registro inválidos"
                    else -> "Error al registrar usuario"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Inicia sesión con la API
     */
    suspend fun login(email: String, password: String): Result<AuthData> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar token y datos de usuario
                sessionManager.saveAuthToken(authResponse.data.accessToken)
                sessionManager.saveUserData(
                    userId = authResponse.data.user.id,
                    email = authResponse.data.user.email,
                    role = authResponse.data.user.role
                )

                // Retornar los datos completos de autenticación
                Result.success(authResponse.data)
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Credenciales incorrectas"
                    404 -> "Usuario no encontrado"
                    else -> "Error al iniciar sesión"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene el perfil del usuario autenticado
     */
    suspend fun getProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()

            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                val userDto = profileResponse.data

                // Retornar el User del DTO directamente
                Result.success(userDto)
            } else {
                Result.failure(Exception("Error al obtener perfil"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Cierra sesión
     */
    suspend fun logout() {
        sessionManager.clearAllData()
    }

    /**
     * Verifica si el usuario está autenticado
     */
    suspend fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    /**
     * Obtiene el token guardado
     */
    suspend fun getAuthToken(): String? {
        return sessionManager.getAuthToken()
    }
}