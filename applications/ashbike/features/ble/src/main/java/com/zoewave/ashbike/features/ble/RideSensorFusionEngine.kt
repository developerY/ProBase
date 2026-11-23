package com.zoewave.ashbike.features.ble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RideSensorFusionEngine {
    private val _speedKph = MutableStateFlow(0.0)
    val speedKph: StateFlow<Double> = _speedKph.asStateFlow()
}
