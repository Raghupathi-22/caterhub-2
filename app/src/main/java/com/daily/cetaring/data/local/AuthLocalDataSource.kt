package com.daily.cetaring.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthLocalDataSource(private val context: Context) {

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val ROLES = stringPreferencesKey("roles")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCESS_TOKEN]
    }

    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REFRESH_TOKEN]
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USERNAME]
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }

    val emailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EMAIL]
    }

    val firstNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_NAME]
    }

    val lastNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_NAME]
    }

    val phoneNumberFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PHONE_NUMBER]
    }

    val rolesFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ROLES]
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN]
        }.first()
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN]
        }.first()
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = accessToken
            preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUserData(
        userId: String,
        username: String,
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        roles: List<String> = emptyList()
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.EMAIL] = email ?: ""
            firstName?.let { preferences[PreferencesKeys.FIRST_NAME] = it }
            lastName?.let { preferences[PreferencesKeys.LAST_NAME] = it }
            phoneNumber?.let { preferences[PreferencesKeys.PHONE_NUMBER] = it }
            preferences[PreferencesKeys.ROLES] = roles.joinToString(",")
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
