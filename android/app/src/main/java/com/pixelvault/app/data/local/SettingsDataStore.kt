package com.pixelvault.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        private val KEY_ML_ENABLED = booleanPreferencesKey("ml_enabled")
        private val KEY_REMOTE_SYNC_ENABLED = booleanPreferencesKey("remote_sync_enabled")
        private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"
        private const val DEFAULT_AUTH_TOKEN = "dev-token"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { settings ->
        settings[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BASE_URL] ?: DEFAULT_BASE_URL
    }

    val authToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN] ?: DEFAULT_AUTH_TOKEN
    }

    val useDynamicColors: Flow<Boolean> = context.dataStore.data.map { it[KEY_USE_DYNAMIC_COLORS] ?: false }

    val mlEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_ML_ENABLED] ?: true }

    val remoteSyncEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMOTE_SYNC_ENABLED] ?: false }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { settings ->
            settings[KEY_THEME_MODE] = mode
        }
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

    suspend fun setUseDynamicColors(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun setMlEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ML_ENABLED] = enabled
        }
    }

    suspend fun setRemoteSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMOTE_SYNC_ENABLED] = enabled
        }
    }

    private val KEY_LAST_PROCESSED_AT = stringPreferencesKey("last_processed_at")

    val lastProcessedAt: Flow<String?> = context.dataStore.data.map { it[KEY_LAST_PROCESSED_AT] }

    suspend fun setLastProcessedAt(timestamp: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_PROCESSED_AT] = timestamp
        }
    }
}
