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
import com.zoewave.probase.features.glass.vision.ui.manager.SimpleGlassesCameraManager
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
    val discoveredCameras: List<Pair<String, String>> = emptyList(),
    val logs: List<String> = emptyList(),
    val error: String? = null
)

sealed interface VisionUiEvent {
    data class CheckPermissions(val context: android.content.Context) : VisionUiEvent
    data object TriggerCapture : VisionUiEvent
    data class UpdatePermissionStatus(val granted: Boolean) : VisionUiEvent
    data class UpdateGlassesPermissionStatus(val granted: Boolean) : VisionUiEvent
    data object RequestGlassesPermission : VisionUiEvent
}

@ExperimentalLensFacing
@ExperimentalCamera2Interop
@androidx.xr.projected.experimental.ExperimentalProjectedApi
@HiltViewModel
class VisionViewModel @Inject constructor(
    private val settings: AiConfigurationSettings,
    private val repository: VisionRepository,
    private val bridgeRepository: GlassBridgeRepository,
    val cameraManager: SimpleGlassesCameraManager
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
        cameraManager.discoveredCameras,
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
            discoveredCameras = @Suppress("UNCHECKED_CAST") (args[8] as List<Pair<String, String>>),
            logs = @Suppress("UNCHECKED_CAST") (args[9] as List<String>),
            error = args[10] as String?
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

    private fun checkInitialPermissions(context: android.content.Context) {
        val phoneGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        _isPermissionGranted.value = phoneGranted
        cameraManager.addLog("Phone Camera: ${if (phoneGranted) "GRANTED" else "DENIED"}")
        
        try {
            val glassesContext = androidx.xr.projected.ProjectedContext.createProjectedDeviceContext(context)
            val glassesGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                glassesContext, android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            _isGlassesPermissionGranted.value = glassesGranted
            cameraManager.addLog("Glasses Camera: ${if (glassesGranted) "GRANTED" else "DENIED"}")
        } catch (e: Exception) {
            cameraManager.addLog("Glasses Permission Check Error: ${e.message}")
            _isGlassesPermissionGranted.value = false
        }
    }

    fun onEvent(event: VisionUiEvent) {
        when (event) {
            is VisionUiEvent.CheckPermissions -> checkInitialPermissions(event.context)
            is VisionUiEvent.TriggerCapture -> triggerGlassesCapture()
            is VisionUiEvent.UpdatePermissionStatus -> _isPermissionGranted.value = event.granted
            is VisionUiEvent.UpdateGlassesPermissionStatus -> _isGlassesPermissionGranted.value = event.granted
            is VisionUiEvent.RequestGlassesPermission -> {
                // This will be handled by the Route/Screen via a separate callback if needed, 
                // but we can log the intent here.
                cameraManager.addLog("Intent: Request Glasses Permission")
            }
        }
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

    private fun triggerGlassesCapture() {
        viewModelScope.launch {
            cameraManager.addLog("Sending Remote Command: CAPTURE_IMAGE...")
            bridgeRepository.sendGlassCommand("CAPTURE_IMAGE")
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
