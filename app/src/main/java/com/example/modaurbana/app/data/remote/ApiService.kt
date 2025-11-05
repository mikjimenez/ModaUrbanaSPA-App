package com.example.modaurbana.app.data.remote

import com.example.modaurbana.app.data.remote.dto.*
import retrofit2.http.*


/**
 * Define los endpoints de tu API
 * Usando DummyJSON como ejemplo de API REST con autenticación JWT
 */
interface ApiService {

    /**
     * 🔐 LOGIN - Autenticar usuario
     * POST /user/login
     *
     * Ejemplo de uso:
     * val response = apiService.login(LoginRequest("emilys", "emilyspass"))
     * sessionManager.saveAuthToken(response.accessToken)
     */
    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * 👤 OBTENER USUARIO ACTUAL (requiere autenticación)
     * GET /user/me
     *
     * ⚠️ IMPORTANTE: Este endpoint REQUIERE el token JWT
     * El AuthInterceptor lo añade automáticamente
     *
     * Ejemplo de uso:
     * val currentUser = apiService.getCurrentUser()
     */
    @GET("user/me")
    suspend fun getCurrentUser(): UserDto

    /**
     * 📋 OBTENER LISTA DE USUARIOS
     * GET /users
     *
     * Ejemplo de uso:
     * val response = apiService.getUsers()
     * val usersList = response.users  // Lista de UserDto
     */
    @GET("users")
    suspend fun getUsers(): UsersResponse

    /**
     * 🔍 BUSCAR USUARIOS POR NOMBRE
     * GET /users/search?q={query}
     *
     * Ejemplo de uso:
     * val results = apiService.searchUsers("John")
     */
    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): UsersResponse

    /**
     * 👤 OBTENER USUARIO POR ID
     * GET /users/{id}
     *
     * Ejemplo de uso:
     * val user = apiService.getUserById(1)
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto
}