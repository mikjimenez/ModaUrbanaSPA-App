package com.example.modaurbana.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_IS_LOGGED_IN = stringPreferencesKey("is_logged_in")
    }

    suspend fun saveUserSession(userId: Int, email: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USER_NAME] = name
            preferences[KEY_IS_LOGGED_IN] = "true"
        }
    }

    suspend fun getUserId(): Int? {
        return context.dataStore.data
            .map { preferences -> preferences[KEY_USER_ID] }
            .first()
    }

    fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences -> preferences[KEY_IS_LOGGED_IN] == "true" }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}