package com.zoewave.probase.features.smartcapture.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.smartcapture.data.ImageLoader
import com.zoewave.probase.features.smartcapture.data.SmartCaptureCoordinator
import com.zoewave.probase.features.smartcapture.ui.state.SmartCaptureUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartCaptureViewModel @Inject constructor(
    private val coordinator: SmartCaptureCoordinator,
    private val imageLoader: ImageLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartCaptureUiState())
    val uiState: StateFlow<SmartCaptureUiState> = _uiState.asStateFlow()

    fun setCameraVisible(show: Boolean) {
        _uiState.update { it.copy(showCamera = show) }
    }

    fun onImageCaptured(uriString: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, showCamera = false, errorMessage = null) }
            val bitmap = imageLoader.loadBitmap(uriString)
            if (bitmap != null) {
                processImage(bitmap)
            } else {
                _uiState.update { it.copy(isProcessing = false, errorMessage = "Failed to load image") }
            }
        }
    }

    private suspend fun processImage(bitmap: Bitmap) {
        try {
            val task = coordinator.processImage(bitmap)
            _uiState.update { it.copy(isProcessing = false, capturedTask = task) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isProcessing = false, errorMessage = e.message ?: "Failed to process image") }
        }
    }

    fun reset() {
        _uiState.update { SmartCaptureUiState() }
    }
}
