package com.zoewave.probase.features.ai.local

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSandboxViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val ocrEngine = SandboxOcrEngine()
    private val nanoEngine = SandboxNanoEngine(context)

    private val _uiState = MutableStateFlow("Ready to test.")
    val uiState: StateFlow<String> = _uiState.asStateFlow()

    fun processTestImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = "1. Running Local ML Kit OCR..."
            val rawText = ocrEngine.extractTextFromBitmap(bitmap)
            
            if (rawText.startsWith("OCR_FAILED")) {
                _uiState.value = rawText
                return@launch
            }

            _uiState.value = "2. OCR Success! Length: ${rawText.length} chars.\nSpinning up Gemini Nano..."
            
            val cleanData = nanoEngine.cleanOcrText(rawText)
            
            _uiState.value = "3. Final Result:\n\n$cleanData"
        }
    }
}
