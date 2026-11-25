package com.zoewave.ashbike.mobile.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.ashbike.features.ble.BleOobHandoverData
import com.zoewave.ashbike.features.ble.BleSensorHub
import com.zoewave.ashbike.features.ble.NfcWriteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BleSensorsEvent {
    object StartScan : BleSensorsEvent()
    object StopScan : BleSensorsEvent()
    data class ConnectFromOob(val oobData: BleOobHandoverData) : BleSensorsEvent()
    data class Connect(val macAddress: String) : BleSensorsEvent()
    data class Forget(val macAddress: String) : BleSensorsEvent()
    data class WriteNfcTag(val macAddress: String) : BleSensorsEvent()
}

@HiltViewModel
class BleSensorsViewModel @Inject constructor(
    private val bleSensorHub: BleSensorHub
) : ViewModel() {

    private val _uiState = MutableStateFlow(BleSensorsUiState())
    val uiState: StateFlow<BleSensorsUiState> = _uiState.asStateFlow()

    fun onEvent(event: BleSensorsEvent) {
        when (event) {
            is BleSensorsEvent.StartScan -> {
                // Handle Start Scan
            }
            is BleSensorsEvent.StopScan -> {
                // Handle Stop Scan
            }
            is BleSensorsEvent.ConnectFromOob -> {
                viewModelScope.launch {
                    bleSensorHub.connectFromOob(event.oobData)
                }
            }
            is BleSensorsEvent.Connect -> {
                viewModelScope.launch {
                    bleSensorHub.connect(event.macAddress)
                }
            }
            is BleSensorsEvent.Forget -> {
                // Handle Forget
            }
            is BleSensorsEvent.WriteNfcTag -> {
                // Handle Write Tag
            }
        }
    }
}

data class BleSensorsUiState(
    val isScanning: Boolean = false,
    val connectedSensors: List<String> = emptyList(),
    val discoveredSensors: List<String> = emptyList()
)
