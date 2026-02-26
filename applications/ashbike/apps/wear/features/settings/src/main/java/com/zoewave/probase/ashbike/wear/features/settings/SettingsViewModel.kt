package com.zoewave.probase.ashbike.wear.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppSettingsRepository // Injecting the shared repo!
) : ViewModel() {

    val isAutoPauseEnabled: Flow<Boolean> = repository.unitsFlow.map ({ it == "HeathConnect" })//repository.autoPauseFlow
    val isHealthConnectEnabled: Flow<Boolean> = repository.unitsFlow.map { it == "HeathConnect" }

    // Map the String from the shared repo to a Boolean for the Wear ToggleChip
    val isMetricUnits: Flow<Boolean> = repository.unitsFlow.map { it == "Metric (SI)" }

    fun setAutoPause(enabled: Boolean) {
        viewModelScope.launch {  }
    }

    fun setHealthConnect(enabled: Boolean) {
        viewModelScope.launch {  }
    }

    fun setMetricUnits(isMetric: Boolean) {
        viewModelScope.launch {
            // Convert the boolean back to the String the shared repo expects
            val unitString = if (isMetric) "Metric (SI)" else "Imperial"
            repository.setUnits(unitString)
        }
    }
}