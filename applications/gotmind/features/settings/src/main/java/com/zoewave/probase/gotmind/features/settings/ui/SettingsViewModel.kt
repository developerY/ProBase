package com.zoewave.probase.gotmind.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.gotmind.data.repository.AppSettingsRepository
import com.zoewave.probase.gotmind.analytics.AnalyticsHelper
import com.zoewave.probase.gotmind.model.AppTheme
import com.zoewave.probase.gotmind.model.ColorPalette
import com.zoewave.probase.gotmind.model.MindWaveMode
import com.zoewave.probase.gotmind.model.InstrumentType
import com.zoewave.probase.gotmind.model.ThemeSettings
import com.zoewave.probase.gotmind.model.GameSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    data class SetTheme(val theme: AppTheme) : SettingsEvent
    data class SetPalette(val palette: ColorPalette) : SettingsEvent
    data class SetMindWaveMode(val mode: MindWaveMode) : SettingsEvent
    data class SetInstrument(val instrument: InstrumentType) : SettingsEvent
    data class SetSongMaster(val enabled: Boolean) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    val themeSettings: StateFlow<ThemeSettings> = appSettingsRepository.themeSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeSettings())

    val gameSettings: StateFlow<GameSettings> = appSettingsRepository.gameSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameSettings())

    private val _firebaseId = MutableStateFlow("")
    val firebaseId: StateFlow<String> = _firebaseId.asStateFlow()

    init {
        viewModelScope.launch {
            _firebaseId.value = analyticsHelper.getFirebaseId()
        }
    }

    fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetTheme -> viewModelScope.launch {
                appSettingsRepository.saveTheme(event.theme)
            }
            is SettingsEvent.SetPalette -> viewModelScope.launch {
                appSettingsRepository.savePalette(event.palette)
            }
            is SettingsEvent.SetMindWaveMode -> viewModelScope.launch {
                appSettingsRepository.saveMindWaveMode(event.mode)
            }
            is SettingsEvent.SetInstrument -> viewModelScope.launch {
                appSettingsRepository.saveInstrumentType(event.instrument)
            }
            is SettingsEvent.SetSongMaster -> viewModelScope.launch {
                appSettingsRepository.saveSongMasterEnabled(event.enabled)
            }
        }
    }
}
