package com.zoewave.probase.seaweed.mobile.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.installations.FirebaseInstallations
import com.zoewave.probase.seaweed.data.TestDataGenerator
import com.zoewave.probase.seaweed.data.UserSettingsRepository
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.ThemeMode
import com.zoewave.probase.seaweed.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val testDataGenerator: TestDataGenerator,
) : ViewModel() {

    private val _firebaseDeviceId = MutableStateFlow<String>("Loading...")

    val uiState: StateFlow<SettingsUiState> = combine(
        userSettingsRepository.getUserSettings(),
        _firebaseDeviceId
    ) { settings, deviceId ->
        SettingsUiState.Success(
            settings = settings,
            firebaseDeviceId = deviceId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState.Loading
    )

    init {
        fetchFirebaseDeviceId()
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

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            val currentSettings = (uiState.value as? SettingsUiState.Success)?.settings ?: UserSettings()
            when (event) {
                is SettingsUiEvent.UpdateTheme -> {
                    userSettingsRepository.saveUserSettings(currentSettings.copy(themeConfig = event.themeConfig))
                }
                is SettingsUiEvent.UpdateThemeMode -> {
                    userSettingsRepository.saveUserSettings(currentSettings.copy(themeMode = event.themeMode))
                }
                is SettingsUiEvent.UpdateIncome -> {
                    userSettingsRepository.saveUserSettings(currentSettings.copy(monthlyIncome = event.income))
                }
                is SettingsUiEvent.GenerateTestData -> {
                    testDataGenerator.generateThreeMonthsOfData()
                }
                is SettingsUiEvent.NavigateTo -> { /* Handled in Route */ }
            }
        }
    }
}

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(
        val settings: UserSettings,
        val firebaseDeviceId: String = ""
    ) : SettingsUiState
}

sealed interface SettingsUiEvent {
    data class UpdateTheme(val themeConfig: SeaweedThemeConfig) : SettingsUiEvent
    data class UpdateThemeMode(val themeMode: ThemeMode) : SettingsUiEvent
    data class UpdateIncome(val income: Double) : SettingsUiEvent
    object GenerateTestData : SettingsUiEvent
    data class NavigateTo(val destination: com.zoewave.probase.seaweed.model.navigation.SeaweedDestination) : SettingsUiEvent
}
