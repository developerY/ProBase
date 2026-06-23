package com.zoewave.probase.features.glass.vision.data

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisionRepository @Inject constructor() {
    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> = _capturedImage.asStateFlow()

    private val _imageDescription = MutableStateFlow("")
    val imageDescription: StateFlow<String> = _imageDescription.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    fun updateCapturedImage(bitmap: Bitmap?) {
        _capturedImage.update { bitmap }
    }

    fun updateImageDescription(description: String) {
        _imageDescription.update { description }
    }

    fun updateAnalyzing(analyzing: Boolean) {
        _isAnalyzing.update { analyzing }
    }

    fun updateCapturing(capturing: Boolean) {
        _isCapturing.update { capturing }
    }

    fun clear() {
        _capturedImage.update { null }
        _imageDescription.update { "" }
        _isAnalyzing.update { false }
        _isCapturing.update { false }
    }
}
