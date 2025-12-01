package com.example.modaurbana.app.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.modaurbana.app.data.remote.ApiService
import com.example.modaurbana.app.data.remote.dto.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pruebas unitarias para AuthSaborLocalRepository
 *
 * Cubre:
 * - Login exitoso
 * - Login con credenciales inválidas
 * - Registro exitoso
 * - Registro con email duplicado
 * - Creación de productor
 * - Obtener todos los usuarios
 */
class AuthSaborLocalRepository {

    // Mocks
    private lateinit var mockContext: Context
    private lateinit var mockApiService: ApiService
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    // SUT (System Under Test)
    private lateinit var repository: AuthSaborLocalRepository

    @Before
    fun setup() {
        // Crear mocks
        mockContext = mockk(relaxed = true)
        mockApiService = mockk()
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        // Configurar comportamiento de SharedPreferences
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockEditor.clear() } returns mockEditor

        // Mockear RetrofitClient para que use nuestro mock
        mockkObject(com.example.modaurbana.app.data.remote.RetrofitClient)
        every { com.example.modaurbana.app.data.remote.RetrofitClient.ApiService } returns mockApiService

        // Crear instancia del repository
        repository = AuthSaborLocalRepository()
    }

    @After
    fun teardown() {
        // Limpiar todos los mocks
        unmockkAll()
    }

    // ==================== LOGIN TESTS ====================

    @Test
    fun `login exitoso debe retornar Success con User`() = runTest {
        // Given - Preparar datos de prueba
        val email = "test@example.com"
        val password = "password123"

        val userDto = UserDto(
            id = "user123",
            name = "Test User",
            email = email,
            role = "CLIENTE",
            telefono = "123456789",
            ubicacion = "Santiago",
            direccion = "Calle Falsa 123"
        )

        val authData = AuthDto(
            user = userDto,
            accessToken = "mock_token_12345"
        )

        val apiResponse = ApiResponse(
            success = true,
            message = "Login exitoso",
            data = authData
        )

        val response = Response.success(apiResponse)

        // Configurar mock para retornar respuesta exitosa
        coEvery {
            mockApiService.login(
                LoginSaborLocalRequest(email, password)
            )
        } returns response

        // When - Ejecutar login
        val result = repository.login(email, password)

        // Then - Verificar resultado
        assertTrue(result.isSuccess, "El resultado debe ser Success")

        val user = result.getOrNull()
        assertEquals("user123", user?.id)
        assertEquals("Test User", user?.nombre)
        assertEquals(email, user?.email)
        assertEquals("CLIENTE", user?.role)

        // Verificar que se guardó el token
        verify { mockEditor.putString("auth_token", "mock_token_12345") }
        verify { mockEditor.putString("user_id", "user123") }
        verify { mockEditor.apply() }
    }

    @Test
    fun `login con credenciales inválidas debe retornar Failure`() = runTest {
        // Given
        val email = "wrong@example.com"
        val password = "wrongpassword"

        val response = Response.error<ApiResponse<AuthDto>>(
            401,
            okhttp3.ResponseBody.create(null, "")
        )

        coEvery {
            mockApiService.login(any())
        } returns response

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isFailure, "El resultado debe ser Failure")
        assertEquals(
            "Credenciales inválidas",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `login con error de red debe retornar Failure con mensaje de error`() = runTest {
        // Given
        coEvery {
            mockApiService.login(any())
        } throws Exception("Network error")

        // When
        val result = repository.login("test@example.com", "password")

        // Then
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("Error de red") == true
        )
    }

    // ==================== REGISTER TESTS ====================

    @Test
    fun `registro exitoso debe retornar Success con User`() = runTest {
        // Given
        val nombre = "Nuevo Usuario"
        val email = "nuevo@example.com"
        val password = "password123"

        val userDto = UserDto(
            id = "newuser123",
            nombre = nombre,
            email = email,
            role = "CLIENTE",
            telefono = null,
            ubicacion = null,
            direccion = null
        )

        val authData = AuthDto(
            user = userDto,
            accessToken = "new_token_12345"
        )

        val apiResponse = ApiResponse(
            success = true,
            data = authData
        )

        val response = Response.success(apiResponse)

        coEvery {
            mockApiService.register(any())
        } returns response

        // When
        val result = repository.register(
            nombre = nombre,
            email = email,
            password = password
        )

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals(nombre, user?.nombre)
        assertEquals(email, user?.email)
        assertEquals("CLIENTE", user?.role)
    }

    @Test
    fun `registro con email duplicado debe retornar Failure`() = runTest {
        // Given
        val response = Response.error<ApiResponse<AuthDto>>(
            409,
            okhttp3.ResponseBody.create(null, "")
        )

        coEvery {
            mockApiService.register(any())
        } returns response

        // When
        val result = repository.register(
            nombre = "Test",
            email = "existing@example.com",
            password = "password123"
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals(
            "El email ya está registrado",
            result.exceptionOrNull()?.message
        )
    }

    // ==================== CREATE PRODUCTOR TESTS ====================

    @Test
    fun `createProductorUser exitoso debe retornar Success`() = runTest {
        // Given
        val userDto = UserDto(
            id = "productor123",
            nombre = "Productor Test",
            email = "productor@example.com",
            role = "PRODUCTOR",
            telefono = "987654321",
            ubicacion = "Valparaíso",
            direccion = null
        )

        val authData = AuthDto(
            user = userDto,
            accessToken = "productor_token"
        )

        val apiResponse = ApiResponse(
            success = true,
            data = authData
        )

        val response = Response.success(apiResponse)

        coEvery {
            mockApiService.createProductorUser(any())
        } returns response

        // When
        val result = repository.createProductorUser(
            nombre = "Productor Test",
            email = "productor@example.com",
            password = "password123",
            ubicacion = "Valparaíso",
            telefono = "987654321"
        )

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals("PRODUCTOR", user?.role)
        assertEquals("Valparaíso", user?.ubicacion)
    }

    @Test
    fun `createProductorUser sin permisos debe retornar Failure`() = runTest {
        // Given - Usuario no ADMIN
        val response = Response.error<ApiResponse<AuthDto>>(
            403,
            okhttp3.ResponseBody.create(null, "")
        )

        coEvery {
            mockApiService.createProductorUser(any())
        } returns response

        // When
        val result = repository.createProductorUser(
            nombre = "Test",
            email = "test@example.com",
            password = "password",
            ubicacion = "Santiago",
            telefono = "123456789"
        )

        // Then
        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("No tienes permisos") == true
        )
    }

    // ==================== GET ALL USERS TESTS ====================

    @Test
    fun `getAllUsers debe retornar lista de usuarios`() = runTest {
        // Given
        val users = listOf(
            UserDto(
                id = "1",
                nombre = "User 1",
                email = "user1@example.com",
                role = "CLIENTE",
                telefono = null,
                ubicacion = null,
                direccion = null
            ),
            UserDto(
                id = "2",
                nombre = "User 2",
                email = "user2@example.com",
                role = "PRODUCTOR",
                telefono = "123456789",
                ubicacion = "Santiago",
                direccion = null
            )
        )

        val apiResponse = ApiResponse(
            success = true,
            data = users
        )

        val response = Response.success(apiResponse)

        coEvery {
            mockApiService.getAllUsers()
        } returns response

        // When
        val result = repository.getAllUsers()

        // Then
        assertTrue(result.isSuccess)
        val userList = result.getOrNull()
        assertEquals(2, userList?.size)
        assertEquals("User 1", userList?.get(0)?.nombre)
        assertEquals("PRODUCTOR", userList?.get(1)?.role)
    }

    // ==================== SESSION TESTS ====================

    @Test
    fun `isLoggedIn debe retornar true cuando hay token`() {
        // Given
        every { mockSharedPreferences.getString("auth_token" வேண்ட
                every { mockSharedPreferences.getString("auth_token", null) } returns "mock_token"

            // When
            val isLoggedIn = repository.isLoggedIn()

            // Then
            assertTrue(isLoggedIn)
        }

        @Test
        fun `isLoggedIn debe retornar false cuando no hay token`() {
            // Given
            every { mockSharedPreferences.getString("auth_token", null) } returns null

            // When
            val isLoggedIn = repository.isLoggedIn()

            // Then
            assertTrue(!isLoggedIn)
        }

        @Test
        fun `logout debe limpiar SharedPreferences`() {
            // When
            repository.logout()

            // Then
            verify { mockEditor.clear() }
            verify { mockEditor.apply() }
        }
    }