package com.zoewave.probase.ashbike.wear.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppSettingsRepository
) : ViewModel() {

    // 1. The Single UI State Flow
    val uiState: StateFlow<SettingsUiState> = repository.unitsFlow.map { unitString ->
        SettingsUiState(
            // Keeping your placeholder logic here for now
            isAutoPauseEnabled = unitString == "HeathConnect",
            isHealthConnectEnabled = unitString == "HeathConnect",
            isMetricUnits = unitString == "Metric (SI)"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    // 2. The Single Event Handler
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleAutoPause -> {
                viewModelScope.launch {
                    // repository.setAutoPause(event.isEnabled)
                }
            }
            is SettingsEvent.ToggleHealthConnect -> {
                viewModelScope.launch {
                    // repository.setHealthConnect(event.isEnabled)
                }
            }
            is SettingsEvent.ToggleMetricUnits -> {
                viewModelScope.launch {
                    val unitString = if (event.isMetric) "Metric (SI)" else "Imperial"
                    repository.setUnits(unitString)
                }
            }
            SettingsEvent.OnAboutClicked -> {
                // Handle any analytics or navigation side-effects here if needed
            }
        }
    }
}