package com.zoewave.probase.features.ai.capture.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartCaptureViewModel @Inject constructor(
    private val orchestrator: SmartCaptureOrchestrator,
    private val settings: SmartCaptureSettings,
    private val imageLoader: ImageLoader,
    private val networkStatsProvider: NetworkStatsProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SmartCaptureUiState>(SmartCaptureUiState.Idle())
    val uiState: StateFlow<SmartCaptureUiState> = _uiState.asStateFlow()

    fun onUserCommentChanged(comment: String) {
        val currentState = _uiState.value
        if (currentState is SmartCaptureUiState.Idle) {
            _uiState.value = currentState.copy(userComment = comment)
        }
    }

    fun analyzePhoto(uriString: String?, userContext: String? = null) {
        viewModelScope.launch(Dispatchers.Default) {
            val netType = networkStatsProvider.getNetworkType()
            _uiState.value = SmartCaptureUiState.Loading(
                logs = listOf("Starting analysis...", "Network: $netType"),
                networkSpeed = netType
            )
            
            val bitmap = uriString?.let { imageLoader.loadBitmap(it) }
            if (bitmap != null || !userContext.isNullOrBlank()) {
                try {
                    val apiKey = settings.userApiKeyFlow.firstOrNull()
                    val modelName = settings.userAiModelFlow.first()
                    val isUsingCloud = !apiKey.isNullOrBlank()
                    
                    val engineDescription = when {
                        bitmap != null && isUsingCloud -> "Cloud ($modelName)"
                        bitmap != null -> "Local (ML Kit)"
                        else -> "Cloud Text-Only"
                    }
                    
                    _uiState.value = SmartCaptureUiState.Loading(
                        logs = listOf(
                            if (bitmap != null) "Image loaded" else "Text context provided", 
                            "Engine: $engineDescription"
                        ),
                        isUsingCloud = isUsingCloud,
                        networkSpeed = netType
                    )

                    val result = orchestrator.processImage(
                        bitmap = bitmap,
                        apiKey = apiKey,
                        modelName = modelName,
                        userContext = userContext,
                        onLog = { newLog ->
                            _uiState.update { currentState ->
                                if (currentState is SmartCaptureUiState.Loading) {
                                    currentState.copy(logs = currentState.logs + newLog)
                                } else {
                                    currentState
                                }
                            }
                        }
                    )
                    
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
                _uiState.value = SmartCaptureUiState.Error("No content provided to analyze.")
            }
        }
    }

    fun analyzePhoto(bitmap: Bitmap, userContext: String? = null) {
        viewModelScope.launch(Dispatchers.Default) {
            val netType = networkStatsProvider.getNetworkType()
            _uiState.value = SmartCaptureUiState.Loading(
                logs = listOf("Direct bitmap provided", "Network: $netType"),
                isUsingCloud = false,
                networkSpeed = netType
            )
            
            try {
                val apiKey = settings.userApiKeyFlow.firstOrNull()
                val modelName = settings.userAiModelFlow.first()
                val isUsingCloud = !apiKey.isNullOrBlank()
                
                _uiState.value = SmartCaptureUiState.Loading(
                    logs = listOf("Processing image...", "Engine: ${if (isUsingCloud) "Cloud ($modelName)" else "Local"}"),
                    isUsingCloud = isUsingCloud,
                    networkSpeed = netType
                )

                val result = orchestrator.processImage(
                    bitmap = bitmap,
                    apiKey = apiKey,
                    modelName = modelName,
                    userContext = userContext,
                    onLog = { newLog ->
                        _uiState.update { currentState ->
                            if (currentState is SmartCaptureUiState.Loading) {
                                currentState.copy(logs = currentState.logs + newLog)
                            } else {
                                currentState
                            }
                        }
                    }
                )
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

    fun setCapturedUri(uri: String) {
        _uiState.value = SmartCaptureUiState.Idle(capturedUri = uri)
    }

    fun reset() {
        _uiState.value = SmartCaptureUiState.Idle()
    }
}
