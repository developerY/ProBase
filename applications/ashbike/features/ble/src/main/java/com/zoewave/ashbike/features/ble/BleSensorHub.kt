package com.zoewave.ashbike.features.ble

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SensorDeviceState(
    val isConnected: Boolean = false,
    val batteryPercent: Int? = null,
    val supportedProfiles: Set<String> = emptySet()
)

class BleSensorHub {
    private val _deviceStates = MutableStateFlow<Map<String, SensorDeviceState>>(emptyMap())
    val deviceStates: StateFlow<Map<String, SensorDeviceState>> = _deviceStates.asStateFlow()

    // Stub for future task 2 full GATT session multiplexing implementation
    fun connect(macAddress: String) {
        // Handle connections
    }
}
