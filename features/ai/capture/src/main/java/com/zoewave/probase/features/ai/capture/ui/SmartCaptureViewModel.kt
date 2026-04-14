package com.zoewave.probase.features.ai.capture.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.core.util.network.NetworkStatsProvider
import com.zoewave.probase.features.ai.capture.data.ImageLoader
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.features.ai.capture.ui.state.SmartCaptureUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartCaptureViewModel @Inject constructor(
    private val orchestrator: SmartCaptureOrchestrator,
    private val settings: SmartCaptureSettings,
    private val imageLoader: ImageLoader,
    private val networkStatsProvider: NetworkStatsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<SmartCaptureUiState>(SmartCaptureUiState.Idle)
    val uiState: StateFlow<SmartCaptureUiState> = _uiState.asStateFlow()

    fun analyzePhoto(uriString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val netType = networkStatsProvider.getNetworkType()
            _uiState.value = SmartCaptureUiState.Loading(
                logs = listOf("Starting analysis...", "Network: $netType"),
                networkSpeed = netType
            )
            
            val bitmap = imageLoader.loadBitmap(uriString)
            if (bitmap != null) {
                try {
                    val apiKey = settings.userApiKeyFlow.firstOrNull()
                    val modelName = settings.userAiModelFlow.firstOrNull()
                    val isUsingCloud = !apiKey.isNullOrBlank()
                    
                    _uiState.value = SmartCaptureUiState.Loading(
                        logs = listOf("Image loaded", "Engine: ${if (isUsingCloud) "Cloud ($modelName)" else "Local (ML Kit)"}"),
                        isUsingCloud = isUsingCloud,
                        networkSpeed = netType
                    )

                    val result = orchestrator.processImage(bitmap, apiKey, modelName)
                    
                    if (result.error != null) {
                        _uiState.value = SmartCaptureUiState.Error(result.error, result.logs)
                    } else {
                        _uiState.value = SmartCaptureUiState.Success(
                            draft = result.draft.copy(photoUri = uriString),
                            engineUsed = result.engineUsed,
                            diagnostics = result.logs,
                            warnings = result.warnings
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = SmartCaptureUiState.Error(e.message ?: "Unknown error occurred")
                }
            } else {
                _uiState.value = SmartCaptureUiState.Error("Failed to load captured image.")
            }
        }
    }

    fun analyzePhoto(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val netType = networkStatsProvider.getNetworkType()
            _uiState.value = SmartCaptureUiState.Loading(
                logs = listOf("Direct bitmap provided", "Network: $netType"),
                isUsingCloud = false,
                networkSpeed = netType
            )
            
            try {
                val apiKey = settings.userApiKeyFlow.firstOrNull()
                val isUsingCloud = !apiKey.isNullOrBlank()
                
                _uiState.value = SmartCaptureUiState.Loading(
                    logs = listOf("Processing image...", "Engine: ${if (isUsingCloud) "Cloud" else "Local"}"),
                    isUsingCloud = isUsingCloud,
                    networkSpeed = netType
                )

                val result = orchestrator.processImage(bitmap, apiKey)
                _uiState.value = SmartCaptureUiState.Success(
                    draft = result.draft,
                    engineUsed = result.engineUsed,
                    diagnostics = result.logs,
                    warnings = result.warnings
                )
            } catch (e: Exception) {
                _uiState.value = SmartCaptureUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun reset() {
        _uiState.value = SmartCaptureUiState.Idle
    }
}
