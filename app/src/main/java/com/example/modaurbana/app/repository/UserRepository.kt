package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.AppDatabase
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.local.entity.UserEntity
import com.example.modaurbana.app.data.remote.ApiService
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.UserDto

class UserRepository(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val sessionManager = SessionManager(context)
    private val apiService: ApiService = RetrofitClient
        .create(context)
        .create(ApiService::class.java)

    /**
     * Registra un nuevo usuario
     */
    suspend fun register(email: String, password: String, name: String): Result<UserEntity> {
        return try {
            // Verificar si el email ya existe
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("El email ya está registrado"))
            }

            // Crear nuevo usuario
            val newUser = UserEntity(
                email = email,
                password = password,
                name = name
            )

            val userId = userDao.insertUser(newUser)
            val createdUser = newUser.copy(id = userId.toInt())

            // Guardar sesión
            sessionManager.saveUserSession(createdUser.id, createdUser.email, createdUser.name)

            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión
     */
    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.login(email, password)

            if (user != null) {
                sessionManager.saveUserSession(user.id, user.email, user.name)
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario actual
     */
    suspend fun getCurrentUser(): Result<UserEntity?> {
        return try {
            val userId = sessionManager.getUserId()
            if (userId != null) {
                val user = userDao.getUserById(userId)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la foto de perfil
     */
    suspend fun updateAvatar(userId: Int, avatarUri: String): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(avatarUri = avatarUri)
                userDao.updateUser(updatedUser)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra sesion
     */
    suspend fun logout() {
        sessionManager.clearSession()
    }
    suspend fun fetchUser(id: Int = 1): Result<UserDto> {
        return try {
            // Llamar a la API (esto puede tardar varios segundos)
            val user = apiService.getUserById(id)

            // Retornar éxito
            Result.success(user)

        } catch (e: Exception) {
            // Si algo falla (sin internet, timeout, etc.)
            Result.failure(e)
        }
    }

}