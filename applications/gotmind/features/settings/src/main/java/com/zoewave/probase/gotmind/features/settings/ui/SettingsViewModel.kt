package com.zoewave.probase.gotmind.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.gotmind.data.repository.AppSettingsRepository
import com.zoewave.probase.gotmind.model.AppTheme
import com.zoewave.probase.gotmind.model.ColorPalette
import com.zoewave.probase.gotmind.model.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    data class SetTheme(val theme: AppTheme) : SettingsEvent
    data class SetPalette(val palette: ColorPalette) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val themeSettings: StateFlow<ThemeSettings> = appSettingsRepository.themeSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeSettings())

    fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetTheme -> viewModelScope.launch {
                appSettingsRepository.saveTheme(event.theme)
            }
            is SettingsEvent.SetPalette -> viewModelScope.launch {
                appSettingsRepository.savePalette(event.palette)
            }
        }
    }
}
