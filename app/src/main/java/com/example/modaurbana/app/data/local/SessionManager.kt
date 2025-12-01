package com.example.modaurbana.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    // ==================== TOKEN ====================

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    suspend fun getAuthToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[AUTH_TOKEN_KEY]
    }

    fun getAuthTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    }

    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }

    // ==================== USER DATA ====================

    suspend fun saveUserData(
        userId: String,
        email: String,
        role: String,
        name: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_ROLE_KEY] = role
            name?.let { preferences[USER_NAME_KEY] = it }
        }
    }

    suspend fun getUserId(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[USER_ID_KEY]
    }

    suspend fun getUserEmail(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[USER_EMAIL_KEY]
    }

    suspend fun getUserRole(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[USER_ROLE_KEY]
    }

    suspend fun getUserName(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[USER_NAME_KEY]
    }

    // ==================== COMPLETE SESSION ====================

    data class UserSession(
        val token: String?,
        val userId: String?,
        val email: String?,
        val role: String?,
        val name: String?
    )

    fun getUserSessionFlow(): Flow<UserSession> {
        return context.dataStore.data.map { preferences ->
            UserSession(
                token = preferences[AUTH_TOKEN_KEY],
                userId = preferences[USER_ID_KEY],
                email = preferences[USER_EMAIL_KEY],
                role = preferences[USER_ROLE_KEY],
                name = preferences[USER_NAME_KEY]
            )
        }
    }

    suspend fun getUserSession(): UserSession {
        val preferences = context.dataStore.data.first()
        return UserSession(
            token = preferences[AUTH_TOKEN_KEY],
            userId = preferences[USER_ID_KEY],
            email = preferences[USER_EMAIL_KEY],
            role = preferences[USER_ROLE_KEY],
            name = preferences[USER_NAME_KEY]
        )
    }

    /**
     * Verifica si hay una sesión activa (si existe un token)
     */
    suspend fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    /**
     * Retorna Flow para observar cambios en el estado de login
     */
    fun isLoggedInFlow(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY] != null
        }
    }

    /**
     * Limpia todos los datos de sesión
     */
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Limpia solo la sesión (alias de clearAllData para mantener compatibilidad)
     */
    suspend fun clearSession() {
        clearAllData()
    }
}