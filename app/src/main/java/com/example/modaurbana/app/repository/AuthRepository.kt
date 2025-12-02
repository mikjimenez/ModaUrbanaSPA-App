package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.*

/**
 * Repositorio para manejar autenticación con la API
 * Implementación defensiva: evita hacer IO/initialization peligrosa en el constructor.
 */
class AuthRepository(private val context: Context) {

    // Lazy init y protegido: si la inicialización falla, queda null en vez de lanzar
    private val apiService by lazy {
        try {
            RetrofitClient.apiService
        } catch (t: Throwable) {
            // Opcional: Log.e("AuthRepository", "ApiService init failed", t)
            null
        }
    }

    private val sessionManager by lazy {
        try {
            SessionManager(context)
        } catch (t: Throwable) {
            // Opcional: Log.e("AuthRepository", "SessionManager init failed", t)
            null
        }
    }

    private fun checkDeps(): Exception? {
        return when {
            apiService == null -> Exception("Dependencia no inicializada: ApiService")
            sessionManager == null -> Exception("Dependencia no inicializada: SessionManager")
            else -> null
        }
    }

    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        telefono: String? = null,
        direccion: String? = null,
        tallas: String? = null,
        preferencias: List<String>? = null
    ): Result<AuthData> {
        checkDeps()?.let { return Result.failure(it) }

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

            val response = apiService!!.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar token y datos de usuario en DataStore (si sessionManager existe)
                sessionManager?.let { sm ->
                    try {
                        sm.saveAuthToken(authResponse.data.accessToken)
                        sm.saveUserData(
                            userId = authResponse.data.user.id,
                            email = authResponse.data.user.email,
                            role = authResponse.data.user.role
                        )
                    } catch (t: Throwable) {
                        // No hacemos crash por fallo al salvar sesión, sólo lo reportamos en el resultado
                        return Result.failure(Exception("Registrado OK pero no se pudo guardar sesión: ${t.message}"))
                    }
                }

                Result.success(authResponse.data)
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "El email ya está registrado"
                    400 -> "Datos de registro inválidos"
                    else -> "Error al registrar usuario (HTTP ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun login(email: String, password: String): Result<AuthData> {
        checkDeps()?.let { return Result.failure(it) }

        return try {
            val request = LoginRequest(email, password)
            val response = apiService!!.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                try {
                    sessionManager?.saveAuthToken(authResponse.data.accessToken)
                    sessionManager?.saveUserData(
                        userId = authResponse.data.user.id,
                        email = authResponse.data.user.email,
                        role = authResponse.data.user.role
                    )
                } catch (t: Throwable) {
                    return Result.failure(Exception("Login OK pero fallo al guardar sesión: ${t.message}"))
                }

                Result.success(authResponse.data)
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Credenciales incorrectas"
                    404 -> "Usuario no encontrado"
                    else -> "Error al iniciar sesión (HTTP ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getProfile(): Result<User> {
        checkDeps()?.let { return Result.failure(it) }

        return try {
            val response = apiService!!.getProfile()

            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                Result.success(profileResponse.data)
            } else {
                Result.failure(Exception("Error al obtener perfil (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        checkDeps()?.let { return Result.failure(it) }

        return try {
            val response = apiService!!.getAllUsers()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener usuarios (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun logout() {
        try {
            sessionManager?.clearAllData()
        } catch (_: Throwable) {
            // swallow
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return try {
            sessionManager?.isLoggedIn() ?: false
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun getAuthToken(): String? {
        return try {
            sessionManager?.getAuthToken()
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getUserSession(): SessionManager.UserSession? {
        return try {
            sessionManager?.getUserSession()
        } catch (_: Throwable) {
            null
        }
    }
}
