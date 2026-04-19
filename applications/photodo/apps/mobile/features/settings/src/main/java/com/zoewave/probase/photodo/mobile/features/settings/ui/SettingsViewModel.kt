package com.zoewave.probase.photodo.mobile.features.settings.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.installations.FirebaseInstallations
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import com.zoewave.probase.features.compliance.AgeSignalsManager
import com.zoewave.probase.features.compliance.model.AgeVerificationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val ageSignalsManager: AgeSignalsManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private data class UiFlags(
        val initialExpandedKey: String? = null,
        val isThemeExpanded: Boolean = false,
        val isAiExpanded: Boolean = false,
        val isAboutExpanded: Boolean = false
    )

    private val _uiFlags = MutableStateFlow(UiFlags())

    // NEW: Firebase Device ID state
    private val _firebaseDeviceId = MutableStateFlow<String>("Loading...")

    // NEW: Compliance status state
    private val _ageVerificationStatus = MutableStateFlow<String>("Not applicable")
    private val _isAgeVerified = MutableStateFlow<Boolean>(true)

    // Combines the DB theme preference with the navigation argument into a single UI State
@Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsRepository.themePreferenceFlow,
        appSettingsRepository.palettePreferenceFlow,
        appSettingsRepository.paneContrastFlow,
        appSettingsRepository.isGeminiApiKeySetFlow,
        appSettingsRepository.isAiEnabledFlow,
        appSettingsRepository.animationsEnabledFlow,
        _firebaseDeviceId,
        _ageVerificationStatus,
        _isAgeVerified,
        _uiFlags
    ) { args: Array<Any?> ->
        val flags = args[9] as UiFlags
        
        // Auto-expand logic (only once on init)
        val finalThemeExpanded = flags.isThemeExpanded || (flags.initialExpandedKey == "SYSTEM")
        val finalAiExpanded = flags.isAiExpanded || (flags.initialExpandedKey == "AI")
        val finalAboutExpanded = flags.isAboutExpanded || (flags.initialExpandedKey == "ABOUT")

        SettingsUiState(
            currentTheme = args[0] as String,
            currentPalette = args[1] as String,
            currentPaneContrast = args[2] as String,
            isApiKeySet = args[3] as Boolean,
            isAiEnabled = args[4] as Boolean,
            animationsEnabled = args[5] as Boolean,
            initialCardKeyToExpand = flags.initialExpandedKey,
            appVersion = getAppVersion(),
            firebaseDeviceId = args[6] as String,
            ageVerificationStatus = args[7] as String,
            isAgeVerified = args[8] as Boolean,
            isThemeExpanded = finalThemeExpanded,
            isAiExpanded = finalAiExpanded,
            isAboutExpanded = finalAboutExpanded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        fetchFirebaseDeviceId()
        // Disabled for PhotoDo mobile app as per user request
        // fetchComplianceStatus()
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

    private fun fetchComplianceStatus() {
        viewModelScope.launch {
            val result = ageSignalsManager.getAgeSignal()
            result.fold(
                onSuccess = { signal ->
                    val rangeText = signal.ageRange?.description ?: "Unknown"
                    val statusText = signal.verificationStatus.name
                    _ageVerificationStatus.value = "$rangeText ($statusText)"
                    _isAgeVerified.value = signal.verificationStatus == AgeVerificationStatus.VERIFIED
                },
                onFailure = { error ->
                    _ageVerificationStatus.value = "Error: ${error.message}"
                    _isAgeVerified.value = false
                }
            )
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
            is SettingsEvent.OnAnimationsEnabledToggled -> {
                viewModelScope.launch { appSettingsRepository.saveAnimationsEnabled(event.enabled) }
            }

            is SettingsEvent.OnThemeExpandedToggled -> {
                _uiFlags.update { it.copy(isThemeExpanded = event.expanded) }
            }
            is SettingsEvent.OnAiExpandedToggled -> {
                _uiFlags.update { it.copy(isAiExpanded = event.expanded) }
            }
            is SettingsEvent.OnAboutExpandedToggled -> {
                _uiFlags.update { it.copy(isAboutExpanded = event.expanded) }
            }
        }
    }

    // Called once when the Route initializes
    fun setInitialExpandedKey(key: String?) {
        if (_uiFlags.value.initialExpandedKey == null && key != null) {
            _uiFlags.update { it.copy(initialExpandedKey = key) }
        }
    }
}
