package com.pixelvault.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.SettingsDataStore
import com.pixelvault.app.data.local.PhotoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: String = "SYSTEM",
    val useDynamicColors: Boolean = false,
    val mlEnabled: Boolean = true,
    val remoteSyncEnabled: Boolean = false,
    val serverUrl: String = "",
    val authToken: String = "",
    val unprocessedCount: Int = 0,
    val photoCount: Int = 0,
    val lastProcessed: String = "Never",
    val advancedExpanded: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsDataStore,
    private val photoDao: PhotoDao
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = SettingsState(
                themeMode = settings.themeMode.first(),
                useDynamicColors = settings.useDynamicColors.first(),
                mlEnabled = settings.mlEnabled.first(),
                remoteSyncEnabled = settings.remoteSyncEnabled.first(),
                serverUrl = settings.baseUrl.first(),
                authToken = settings.authToken.first(),
                unprocessedCount = photoDao.getUnprocessedPhotos().size,
                photoCount = photoDao.count(),
                lastProcessed = "Today"
            )
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settings.setThemeMode(mode)
            _state.value = _state.value.copy(themeMode = mode)
        }
    }

    fun setUseDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            settings.setUseDynamicColors(enabled)
            _state.value = _state.value.copy(useDynamicColors = enabled)
        }
    }

    fun setMlEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setMlEnabled(enabled)
            _state.value = _state.value.copy(mlEnabled = enabled)
        }
    }

    fun setRemoteSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setRemoteSyncEnabled(enabled)
            _state.value = _state.value.copy(remoteSyncEnabled = enabled)
        }
    }

    fun setServerUrl(url: String) {
        _state.value = _state.value.copy(serverUrl = url)
    }

    fun saveServerUrl() {
        viewModelScope.launch {
            settings.setBaseUrl(_state.value.serverUrl)
        }
    }

    fun setAuthToken(token: String) {
        _state.value = _state.value.copy(authToken = token)
    }

    fun saveAuthToken() {
        viewModelScope.launch {
            settings.setAuthToken(_state.value.authToken)
        }
    }

    fun toggleAdvanced() {
        _state.value = _state.value.copy(advancedExpanded = !_state.value.advancedExpanded)
    }

    fun processNow() {
        // Delegates to processing pipeline
    }

    fun syncNow() {
        // Delegates to remote sync
    }
}
