package com.zoewave.probase.features.ai.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
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
        Log.d("AI_SANDBOX", "ViewModel: processTestImage triggered")
        viewModelScope.launch {
            _uiState.value = "1. Running Local ML Kit OCR..."
            val rawText = ocrEngine.extractTextFromBitmap(bitmap)
            
            if (rawText.startsWith("OCR_FAILED")) {
                Log.e("AI_SANDBOX", "ViewModel: Pipeline halted at OCR stage")
                _uiState.value = rawText
                return@launch
            }

            Log.d("AI_SANDBOX", "ViewModel: Handoff raw text to Gemini Nano (length: ${rawText.length})")
            _uiState.value = "2. OCR Success! Length: ${rawText.length} chars.\nSpinning up Gemini Nano..."
            
            val cleanData = nanoEngine.cleanOcrText(rawText)
            
            Log.d("AI_SANDBOX", "ViewModel: Pipeline complete")
            _uiState.value = "3. Final Result:\n\n$cleanData"
        }
    }
}
