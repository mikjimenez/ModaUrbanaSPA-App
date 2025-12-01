package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /**
     * Registra un nuevo usuario usando la API
     */
    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        telefono: String? = null,
        direccion: String? = null
    ) {
        // Validaciones
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val confirmError = validateConfirmPassword(password, confirmPassword)
        val nameError = validateName(name)

        if (emailError != null || passwordError != null || confirmError != null || nameError != null) {
            _uiState.value = _uiState.value.copy(
                emailError = emailError,
                passwordError = passwordError ?: confirmError,
                nameError = nameError,
                isLoading = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            emailError = null,
            passwordError = null,
            nameError = null
        )

        viewModelScope.launch {
            val result = repository.register(
                nombre = name,
                email = email,
                password = password,
                telefono = telefono,
                direccion = direccion
            )

            _uiState.value = result.fold(
                onSuccess = {
                    _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message ?: "Error al registrar"
                    )
                }
            )
        }
    }

    /**
     * Inicia sesión usando la API
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Por favor completa todos los campos"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.login(email, password)

            _uiState.value = result.fold(
                onSuccess = {
                    _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message ?: "Error al iniciar sesión"
                    )
                }
            )
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    // Validaciones (sin cambios)
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
    }
}