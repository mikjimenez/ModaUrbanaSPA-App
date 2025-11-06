package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.local.entity.UserEntity
import com.example.modaurbana.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserEntity? = null,
    val email: UserEntity? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUser()
    }

    /**
     * Carga el usuario actual
     */
    fun loadUser() {
        // Indicar que está cargando
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.getCurrentUser()

            // Actualizar el estado según el resultado
            _uiState.value = result.fold(
                onSuccess = { user ->
                    _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        email = user,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Actualiza la foto de perfil
     */
    fun updateAvatar(avatarUri: String) {
        val userId = _uiState.value.user?.id ?: return

        viewModelScope.launch {
            val result = repository.updateAvatar(userId, avatarUri)

            result.fold(
                onSuccess = {
                    loadUser() // Recargar usuario
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Foto actualizada"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Cierra sesión
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    /**
     * Limpia los mensajes
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}