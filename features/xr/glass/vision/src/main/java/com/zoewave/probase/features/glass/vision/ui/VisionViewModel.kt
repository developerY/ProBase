package com.zoewave.probase.features.glass.vision.ui

import android.graphics.Bitmap
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.ExperimentalLensFacing
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.features.glass.vision.data.VisionRepository
import com.zoewave.probase.features.glass.vision.ui.manager.GlassesCameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisionUiState(
    val imageDescription: String = "",
    val isCapturing: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isApiKeySet: Boolean = false,
    val cameraSource: String = "Phone",
    val isPermissionGranted: Boolean = false,
    val isGlassesPermissionGranted: Boolean = false,
    val capturedImage: Bitmap? = null,
    val logs: List<String> = emptyList(),
    val error: String? = null
)

@ExperimentalLensFacing
@ExperimentalCamera2Interop
@androidx.xr.projected.experimental.ExperimentalProjectedApi
@HiltViewModel
class VisionViewModel @Inject constructor(
    private val settings: AiConfigurationSettings,
    private val repository: VisionRepository,
    private val bridgeRepository: GlassBridgeRepository,
    val cameraManager: GlassesCameraManager
) : ViewModel() {

    private val _isApiKeySet = MutableStateFlow(false)
    private val _isPermissionGranted = MutableStateFlow(false)
    private val _isGlassesPermissionGranted = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VisionUiState> = combine(
        repository.imageDescription,
        repository.isCapturing,
        repository.isAnalyzing,
        _isApiKeySet,
        cameraManager.cameraSource,
        _isPermissionGranted,
        _isGlassesPermissionGranted,
        repository.capturedImage,
        cameraManager.logs,
        _error
    ) { args: Array<Any?> ->
        VisionUiState(
            imageDescription = args[0] as String,
            isCapturing = args[1] as Boolean,
            isAnalyzing = args[2] as Boolean,
            isApiKeySet = args[3] as Boolean,
            cameraSource = args[4] as String,
            isPermissionGranted = args[5] as Boolean,
            isGlassesPermissionGranted = args[6] as Boolean,
            capturedImage = args[7] as Bitmap?,
            logs = @Suppress("UNCHECKED_CAST") (args[8] as List<String>),
            error = args[9] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VisionUiState()
    )

    init {
        checkStatus()
        observeBridgeCommands()
        observeCapturedImages()
    }

    private fun observeBridgeCommands() {
        viewModelScope.launch {
            bridgeRepository.glassCommands.collect { cmd ->
                if (cmd == "CAPTURE_IMAGE") {
                    cameraManager.takePicture()
                }
            }
        }
    }

    private fun observeCapturedImages() {
        viewModelScope.launch {
            repository.capturedImage.collect { bitmap ->
                if (bitmap != null) {
                    analyzeImage(bitmap)
                }
            }
        }
    }

    private fun checkStatus() {
        viewModelScope.launch {
            settings.isGeminiApiKeySetFlow.collect { isSet ->
                _isApiKeySet.value = isSet
            }
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        _isPermissionGranted.value = granted
    }

    fun triggerGlassesCapture() {
        viewModelScope.launch {
            cameraManager.addLog("Sending Remote Command: CAPTURE_IMAGE...")
            bridgeRepository.sendGlassCommand("CAPTURE_IMAGE")
        }
    }

    fun checkGlassesPermission() {
        viewModelScope.launch {
            cameraManager.addLog("Checking Glasses permissions...")
            // The actual check is now handled within the manager's lifecycle
            _isGlassesPermissionGranted.value = true 
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            repository.updateAnalyzing(true)
            try {
                val apiKey = settings.getGeminiApiKey()
                if (apiKey.isNullOrBlank()) {
                    cameraManager.addLog("Error: Gemini API Key missing!")
                    _error.value = "Gemini API Key missing. Check Settings."
                    repository.updateAnalyzing(false)
                    return@launch
                }

                cameraManager.addLog("API Key validated. Model: gemini-1.5-flash")
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )

                val prompt = "Describe this image in a few words for someone wearing AI glasses. Be concise."
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)
                val textResponse = response.text ?: "Could not describe image"
                cameraManager.addLog("Gemini Response: $textResponse")
                
                repository.updateImageDescription(textResponse)
                repository.updateAnalyzing(false)
            } catch (e: Exception) {
                cameraManager.addLog("AI Error: ${e.message}")
                _error.value = "AI Error: ${e.message}"
                repository.updateAnalyzing(false)
            }
        }
    }
}
