package com.zoewave.probase.features.smartcapture.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.features.smartcapture.data.ImageLoader
import com.zoewave.probase.features.smartcapture.data.SmartCaptureOrchestrator
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureSettings
import com.zoewave.probase.features.smartcapture.ui.state.SmartCaptureUiState
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
    private val imageLoader: ImageLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow<SmartCaptureUiState>(SmartCaptureUiState.Idle)
    val uiState: StateFlow<SmartCaptureUiState> = _uiState.asStateFlow()

    fun analyzePhoto(uriString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = SmartCaptureUiState.Loading
            val bitmap = imageLoader.loadBitmap(uriString)
            if (bitmap != null) {
                try {
                    val apiKey = settings.userApiKeyFlow.firstOrNull()
                    val draft = orchestrator.processImage(bitmap, apiKey)
                    _uiState.value = SmartCaptureUiState.Success(draft.copy(photoUri = uriString))
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
            _uiState.value = SmartCaptureUiState.Loading
            
            try {
                val apiKey = settings.userApiKeyFlow.firstOrNull()
                val draft = orchestrator.processImage(bitmap, apiKey)
                _uiState.value = SmartCaptureUiState.Success(draft)
            } catch (e: Exception) {
                _uiState.value = SmartCaptureUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun reset() {
        _uiState.value = SmartCaptureUiState.Idle
    }
}
