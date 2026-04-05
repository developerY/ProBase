package com.zoewave.probase.seaweed.mobile.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.UserSettingsRepository
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.ThemeMode
import com.zoewave.probase.seaweed.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        repository.getUserSettings()
            .onEach { settings ->
                _uiState.value = SettingsUiState.Success(settings)
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            val currentSettings = (uiState.value as? SettingsUiState.Success)?.settings ?: UserSettings()
            when (event) {
                is SettingsUiEvent.UpdateTheme -> {
                    repository.saveUserSettings(currentSettings.copy(themeConfig = event.themeConfig))
                }
                is SettingsUiEvent.UpdateThemeMode -> {
                    repository.saveUserSettings(currentSettings.copy(themeMode = event.themeMode))
                }
                is SettingsUiEvent.UpdateIncome -> {
                    repository.saveUserSettings(currentSettings.copy(monthlyIncome = event.income))
                }
            }
        }
    }
}

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(val settings: UserSettings) : SettingsUiState
}

sealed interface SettingsUiEvent {
    data class UpdateTheme(val themeConfig: SeaweedThemeConfig) : SettingsUiEvent
    data class UpdateThemeMode(val themeMode: ThemeMode) : SettingsUiEvent
    data class UpdateIncome(val income: Double) : SettingsUiEvent
}
