package com.zoewave.probase.features.xr.glass.data

import com.zoewave.probase.features.xr.glass.ui.GlimmerSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlassSessionRepository @Inject constructor() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _activeSample = MutableStateFlow<GlimmerSample?>(null)
    val activeSample: StateFlow<GlimmerSample?> = _activeSample.asStateFlow()

    private val _requestedRoutineTime = MutableStateFlow<String?>(null)
    val requestedRoutineTime: StateFlow<String?> = _requestedRoutineTime.asStateFlow()

    fun updateConnection(connected: Boolean) {
        _isConnected.value = connected
    }

    fun updateActiveSample(sample: GlimmerSample?) {
        _activeSample.value = sample
    }

    fun updateRequestedRoutineTime(time: String?) {
        _requestedRoutineTime.value = time
    }
}
