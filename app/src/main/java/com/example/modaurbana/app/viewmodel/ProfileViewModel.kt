package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.local.entity.UserEntity
import com.example.modaurbana.app.data.remote.dto.ClienteProfile
import com.example.modaurbana.app.repository.AuthRepository
import com.example.modaurbana.app.repository.ClienteProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    // Usuario local (Room)
    val localUser: UserEntity? = null,
    // Perfil del cliente desde API
    val clienteProfile: ClienteProfile? = null,
    // Sesión actual
    val userId: String? = null,
    val email: String? = null,
    val role: String? = null,
    val name: String? = null,
    // Estados
    val error: String? = null,
    val successMessage: String? = null,
    val isUpdatingProfile: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)
    private val authRepository = AuthRepository(application)
    private val clienteProfileRepository = ClienteProfileRepository(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUserData()
    }

    /**
     * Carga todos los datos del usuario
     * - Usuario local (Room)
     * - Sesión actual (DataStore)
     * - Perfil del cliente (API)
     */
    fun loadUserData() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                // 1. Cargar usuario local de Room
                val localUserResult = userRepository.getCurrentUser()

                // 2. Cargar sesión de DataStore
                val session = sessionManager.getUserSession()

                // 3. Intentar cargar perfil del cliente desde API
                val clienteProfileResult = clienteProfileRepository.getMyProfile()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    localUser = localUserResult.getOrNull(),
                    clienteProfile = clienteProfileResult.getOrNull(),
                    userId = session.userId,
                    email = session.email,
                    role = session.role,
                    name = session.name,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    /**
     * Carga solo el usuario local (Room)
     */
    fun loadLocalUser() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = userRepository.getCurrentUser()

            _uiState.value = result.fold(
                onSuccess = { user ->
                    _uiState.value.copy(
                        isLoading = false,
                        localUser = user,
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
     * Carga el perfil del cliente desde la API
     */
    fun loadClienteProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = clienteProfileRepository.getMyProfile()

            _uiState.value = result.fold(
                onSuccess = { profile ->
                    _uiState.value.copy(
                        isLoading = false,
                        clienteProfile = profile,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar perfil: ${exception.message}"
                    )
                }
            )
        }
    }

    /**
     * Actualiza el perfil del cliente en la API
     */
    fun updateClienteProfile(
        nombre: String? = null,
        telefono: String? = null,
        direccion: String? = null,
        tallas: String? = null,
        preferencias: List<String>? = null
    ) {
        _uiState.value = _uiState.value.copy(
            isUpdatingProfile = true,
            error = null
        )

        viewModelScope.launch {
            val result = clienteProfileRepository.updateMyProfile(
                nombre = nombre,
                telefono = telefono,
                direccion = direccion,
                tallas = tallas,
                preferencias = preferencias
            )

            _uiState.value = result.fold(
                onSuccess = { updatedProfile ->
                    _uiState.value.copy(
                        isUpdatingProfile = false,
                        clienteProfile = updatedProfile,
                        successMessage = "Perfil actualizado exitosamente",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isUpdatingProfile = false,
                        error = "Error al actualizar: ${exception.message}"
                    )
                }
            )
        }
    }

    /**
     * Actualiza la foto de perfil (local)
     */
    fun updateAvatar(avatarUri: String) {
        val userId = _uiState.value.localUser?.id ?: return

        viewModelScope.launch {
            val result = userRepository.updateAvatar(userId, avatarUri)

            result.fold(
                onSuccess = {
                    // Recargar usuario local
                    loadLocalUser()
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
     * Obtiene el perfil del usuario autenticado desde la API
     */
    fun getProfileFromApi() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.getProfile()

            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userId = user.id,
                        email = user.email,
                        role = user.role,
                        successMessage = "Perfil cargado",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
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
            try {
                // Limpiar repositorios locales
                userRepository.logout()

                // Limpiar repositorio de autenticación
                authRepository.logout()

                // Resetear estado
                _uiState.value = ProfileUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cerrar sesión: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpia mensajes de éxito y error
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    /**
     * Verifica si el usuario tiene un rol específico
     */
    fun hasRole(role: String): Boolean {
        return _uiState.value.role == role
    }

    /**
     * Verifica si es cliente
     */
    fun isCliente(): Boolean = hasRole("CLIENTE")

    /**
     * Verifica si es admin
     */
    fun isAdmin(): Boolean = hasRole("ADMIN")

    /**
     * Obtiene el nombre para mostrar
     */
    fun getDisplayName(): String {
        return _uiState.value.name
            ?: _uiState.value.clienteProfile?.nombre
            ?: _uiState.value.localUser?.name
            ?: "Usuario"
    }

    /**
     * Obtiene el email para mostrar
     */
    fun getDisplayEmail(): String {
        return _uiState.value.email
            ?: _uiState.value.localUser?.email
            ?: ""
    }
}