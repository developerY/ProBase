package com.zoewave.probase.features.xr.glass.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.xr.glass.data.GlassSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalProjectedApi::class)
@HiltViewModel
class GlassXRDemosViewModel @Inject constructor(
    private val repository: GlassSessionRepository
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = repository.isConnected
    val activeSample: StateFlow<GlimmerSample?> = repository.activeSample

    fun updateActiveSample(sample: GlimmerSample?) {
        repository.updateActiveSample(sample)
    }

    fun checkConnection(context: Context) {
        viewModelScope.launch {
            try {
                val controller = ProjectedDeviceController.create(context)
                repository.updateConnection(controller.capabilities.isNotEmpty())
            } catch (e: Exception) {
                repository.updateConnection(false)
            }
        }
    }
}
