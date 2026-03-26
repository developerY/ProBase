package com.zoewave.probase.photodo.mobile.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    // Holds the deep-link argument passed from the navigation graph
    private val _initialExpandedKey = MutableStateFlow<String?>(null)

    // Combines the DB theme preference with the navigation argument into a single UI State
    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsRepository.themePreferenceFlow,
        _initialExpandedKey
    ) { theme, expandedKey ->
        SettingsUiState(
            currentTheme = theme,
            initialCardKeyToExpand = expandedKey
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnThemeSelected -> {
                viewModelScope.launch {
                    appSettingsRepository.saveThemePreference(event.themeIdentifier)
                }
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
