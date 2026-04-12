package com.zoewave.probase.photodo.mobile.features.settings.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.installations.FirebaseInstallations
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    // Holds the deep-link argument passed from the navigation graph
    private val _initialExpandedKey = MutableStateFlow<String?>(null)

    // NEW: Firebase Device ID state
    private val _firebaseDeviceId = MutableStateFlow<String>("Loading...")

    // Combines the DB theme preference with the navigation argument into a single UI State
    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsRepository.themePreferenceFlow,
        appSettingsRepository.palettePreferenceFlow,
        appSettingsRepository.paneContrastFlow,
        appSettingsRepository.isGeminiApiKeySetFlow,
        appSettingsRepository.isAiEnabledFlow,
        _initialExpandedKey,
        _firebaseDeviceId
    ) { args: Array<Any?> ->
        SettingsUiState(
            currentTheme = args[0] as String,
            currentPalette = args[1] as String,
            currentPaneContrast = args[2] as String,
            isApiKeySet = args[3] as Boolean,
            isAiEnabled = args[4] as Boolean,
            initialCardKeyToExpand = args[5] as String?,
            appVersion = getAppVersion(),
            firebaseDeviceId = args[6] as String
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        fetchFirebaseDeviceId()
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            "${packageInfo.versionName} ($versionCode)"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    private fun fetchFirebaseDeviceId() {
        viewModelScope.launch {
            try {
                val id = FirebaseInstallations.getInstance().id.await()
                _firebaseDeviceId.value = id
            } catch (e: Exception) {
                _firebaseDeviceId.value = "Unavailable"
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnThemeSelected -> {
                viewModelScope.launch {
                    appSettingsRepository.saveThemePreference(event.themeIdentifier)
                }
            }
            is SettingsEvent.OnPaletteSelected -> {
                viewModelScope.launch { appSettingsRepository.savePalettePreference(event.paletteIdentifier) }
            }
            is SettingsEvent.OnPaneContrastSelected -> {
                viewModelScope.launch { appSettingsRepository.savePaneContrast(event.paneContrastIdentifier) }
            }
            is SettingsEvent.OnGeminiApiKeyChanged -> {
                viewModelScope.launch { appSettingsRepository.saveGeminiApiKey(event.apiKey) }
            }
            is SettingsEvent.OnAiEnabledToggled -> {
                viewModelScope.launch { appSettingsRepository.saveAiEnabled(event.enabled) }
            }
        }
    }

    // Called once when the Route initializes
    fun setInitialExpandedKey(key: String?) {
        if (_initialExpandedKey.value == null && key != null) {
            _initialExpandedKey.value = key
        }
    }
}
