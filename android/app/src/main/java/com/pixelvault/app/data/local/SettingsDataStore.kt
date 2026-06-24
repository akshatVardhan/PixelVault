package com.pixelvault.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"
        private const val DEFAULT_AUTH_TOKEN = "dev-token"
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BASE_URL] ?: DEFAULT_BASE_URL
    }

    val authToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN] ?: DEFAULT_AUTH_TOKEN
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = url
        }
    }

    suspend fun setAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
        }
    }
}
