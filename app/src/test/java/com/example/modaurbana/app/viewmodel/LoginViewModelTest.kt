package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.modaurbana.app.repository.AuthSaborLocalRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pruebas unitarias para LoginViewModel
 *
 * Cubre:
 * - Validación de email
 * - Validación de password
 * - Login exitoso
 * - Login fallido
 * - Estados de UI (Loading, Success, Error, Idle)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // Regla para ejecutar código en el mismo thread
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Dispatcher de prueba para coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var mockApplication: Application
    private lateinit var mockRepository: AuthSaborLocalRepository

    // SUT
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        // Configurar dispatcher de prueba
        Dispatchers.setMain(testDispatcher)

        // Crear mocks
        mockApplication = mockk(relaxed = true)
        mockRepository = mockk()

        // Mockear contexto de la aplicación
        every { mockApplication.applicationContext } returns mockApplication

        // TODO: Necesitarás modificar LoginViewModel para inyectar el repository
        // Por ahora, asumiremos que puedes pasar el repository al constructor
        // viewModel = LoginViewModel(mockApplication, mockRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    fun `email vacío debe mostrar error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onEmailChange("")

        // Then
        viewModel.emailError.test {
            val error = awaitItem()
            assertEquals("El email es obligatorio", error)
        }
    }

    @Test
    fun `email inválido debe mostrar error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onEmailChange("invalid-email")

        // Then
        viewModel.emailError.test {
            val error = awaitItem()
            assertTrue(error?.contains("Email inválido") == true)
        }
    }

    @Test
    fun `email válido no debe mostrar error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onEmailChange("valid@example.com")

        // Then
        viewModel.emailError.test {
            val error = awaitItem()
            assertEquals(null, error)
        }
    }

    @Test
    fun `password vacío debe mostrar error`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onPasswordChange("")

        // Then
        viewModel.passwordError.test {
            val error = awaitItem()
            assertEquals("La contraseña es obligatoria", error)
        }
    }

    // ==================== LOGIN TESTS ====================

    @Test
    fun `login exitoso debe cambiar estado a Success`() = runTest {
        // Given
        val mockUser = User(
            id = "123",
            nombre = "Test User",
            email = "test@example.com",
            role = "CLIENTE"
        )

        coEvery {
            mockRepository.login(any(), any())
        } returns Result.success(mockUser)

        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.login()

        // Avanzar el dispatcher para ejecutar coroutines
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is LoginUiState.Success)
            assertEquals(mockUser, (state as LoginUiState.Success).user)
        }
    }

    @Test
    fun `login con credenciales inválidas debe cambiar estado a Error`() = runTest {
        // Given
        coEvery {
            mockRepository.login(any(), any())
        } returns Result.failure(Exception("Credenciales inválidas"))

        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("wrongpassword")
        viewModel.login()

        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is LoginUiState.Error)
            assertEquals(
                "Credenciales inválidas",
                (state as LoginUiState.Error).message
            )
        }
    }

    @Test
    fun `login debe cambiar a Loading y luego a Success`() = runTest {
        // Given
        val mockUser = User(
            id = "123",
            nombre = "Test",
            email = "test@example.com",
            role = "CLIENTE"
        )

        coEvery {
            mockRepository.login(any(), any())
        } coAnswers {
            // Simular delay de red
            kotlinx.coroutines.delay(100)
            Result.success(mockUser)
        }

        viewModel = LoginViewModel(mockApplication)
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        // When - Then
        viewModel.uiState.test {
            // Estado inicial debe ser Idle
            assertEquals(LoginUiState.Idle, awaitItem())

            viewModel.login()

            // Debe cambiar a Loading
            assertEquals(LoginUiState.Loading, awaitItem())

            advanceTimeBy(100)

            // Debe cambiar a Success
            val successState = awaitItem()
            assertTrue(successState is LoginUiState.Success)
        }
    }

    @Test
    fun `login con campos vacíos no debe llamar al repository`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When
        viewModel.login() // Email y password vacíos

        // Then
        coVerify(exactly = 0) {
            mockRepository.login(any(), any())
        }

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is LoginUiState.Error)
        }
    }

    // ==================== PASSWORD VISIBILITY TESTS ====================

    @Test
    fun `togglePasswordVisibility debe cambiar isPasswordVisible`() = runTest {
        // Given
        viewModel = LoginViewModel(mockApplication)

        // When - Then
        viewModel.isPasswordVisible.test {
            assertFalse(awaitItem()) // Inicialmente false

            viewModel.togglePasswordVisibility()
            assertTrue(awaitItem()) // Ahora true

            viewModel.togglePasswordVisibility()
            assertFalse(awaitItem()) // Vuelve a false
        }
    }
}