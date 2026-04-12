package com.zoewave.probase.features.smartcapture.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val coordinator: SmartCaptureCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartCaptureUiState())
    val uiState: StateFlow<SmartCaptureUiState> = _uiState.asStateFlow()

    fun onImageCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            try {
                val task = coordinator.processImage(bitmap)
                _uiState.update { it.copy(isProcessing = false, capturedTask = task) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, errorMessage = e.message ?: "Failed to process image") }
            }
        }
    }

    fun reset() {
        _uiState.update { SmartCaptureUiState() }
    }
}
