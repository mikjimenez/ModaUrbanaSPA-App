package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.local.AppDatabase
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
    val user: UserEntity? = null,
    // Email desde la sesión
    val email: com.example.modaurbana.app.data.remote.dto.User? = null,
    // Perfil del cliente desde API
    val clienteProfile: ClienteProfile? = null,
    // Sesión actual
    val userId: String? = null,
    val role: String? = null,
    val name: String? = null,
    // Estados
    val error: String? = null,
    val successMessage: String? = null,
    val isUpdatingProfile: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val clienteProfileRepository = ClienteProfileRepository(application)
    private val sessionManager = SessionManager(application)
    private val userDao = AppDatabase.getDatabase(application).userDao()

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
    private fun loadUserData() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                // 1. Cargar sesión de DataStore
                val session = sessionManager.getUserSession()

                // 2. Cargar usuario local de Room (si existe)
                val localUser = session.userId?.let { userId ->
                    try {
                        userDao.getUserById(userId.toIntOrNull() ?: 0)
                    } catch (e: Exception) {
                        null
                    }
                }

                // 3. Intentar cargar perfil del cliente desde API
                val clienteProfileResult = try {
                    clienteProfileRepository.getMyProfile()
                } catch (e: Exception) {
                    Result.failure(e)
                }

                // 4. Intentar cargar datos del usuario desde API
                val userFromApi = try {
                    authRepository.getProfile()
                } catch (e: Exception) {
                    Result.failure(e)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = localUser,
                    email = userFromApi.getOrNull(),
                    clienteProfile = clienteProfileResult.getOrNull(),
                    userId = session.userId,
                    role = session.role,
                    name = session.name ?: clienteProfileResult.getOrNull()?.nombre,
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
     * Recarga los datos del usuario
     */
    fun loadUser() {
        loadUserData()
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
        val userId = _uiState.value.user?.id ?: return

        viewModelScope.launch {
            try {
                // Obtener el usuario actual
                val currentUser = userDao.getUserById(userId)

                if (currentUser != null) {
                    // Actualizar con el nuevo avatar
                    val updatedUser = currentUser.copy(avatarUri = avatarUri)
                    userDao.updateUser(updatedUser)

                    // Recargar los datos
                    loadUserData()

                    _uiState.value = _uiState.value.copy(
                        successMessage = "Foto actualizada"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Usuario no encontrado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar foto: ${e.message}"
                )
            }
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
                        email = user,
                        userId = user.id,
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
                // Limpiar sesión
                sessionManager.clearAllData()

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
            ?: _uiState.value.user?.name
            ?: "Usuario"
    }

    /**
     * Obtiene el email para mostrar
     */
    fun getDisplayEmail(): String {
        return _uiState.value.email?.email
            ?: _uiState.value.user?.email
            ?: ""
    }
}