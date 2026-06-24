package com.zoewave.probase.features.glass.vision.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.ComponentActivity
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
import com.zoewave.probase.features.glass.vision.ui.manager.runOfficialCameraTest
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
    val capturedImage: Bitmap? = null,
    val discoveredCameras: List<Pair<String, String>> = emptyList(),
    val logs: List<String> = emptyList(),
    val error: String? = null
)

sealed interface VisionUiEvent {
    data class CheckPermissions(val context: android.content.Context) : VisionUiEvent
    data object TriggerCapture : VisionUiEvent
    data class UpdatePermissionStatus(val granted: Boolean) : VisionUiEvent
    data class RunDiagnostic(val activity: ComponentActivity) : VisionUiEvent
    data class RunOfficialTest(val activity: ComponentActivity) : VisionUiEvent
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

    private val instanceId = java.util.UUID.randomUUID().toString().take(4)
    private val _isApiKeySet = MutableStateFlow(false)
    private val _isPermissionGranted = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private var isCommanderInitialized = false

    val uiState: StateFlow<VisionUiState> = combine(
        repository.imageDescription,
        repository.isCapturing,
        repository.isAnalyzing,
        _isApiKeySet,
        cameraManager.cameraSource,
        _isPermissionGranted,
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
            capturedImage = args[6] as Bitmap?,
            discoveredCameras = @Suppress("UNCHECKED_CAST") (args[7] as List<Pair<String, String>>),
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
    }

    /**
     * Initializes the ViewModel as the primary commander for bridge events.
     * This should ONLY be called by the host Phone UI to avoid duplicate event listeners.
     */
    fun initializeAsHostCommander() {
        if (isCommanderInitialized) {
            Log.d("VisionVM", "[$instanceId] Host Commander already initialized. Skipping.")
            return
        }
        
        Log.d("VisionVM", "[$instanceId] Initializing as Host Commander (Listening to bridge)...")
        isCommanderInitialized = true
        observeBridgeCommands()
        observeCapturedImages()
    }

    private fun checkInitialPermissions(context: android.content.Context) {
        val contextName = context::class.java.simpleName
        val phoneGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        _isPermissionGranted.value = phoneGranted
        val msg = "Camera Permission Status: ${if (phoneGranted) "GRANTED" else "DENIED"} (Checked from $contextName)"
        Log.d("VisionVM", "[$instanceId] $msg")
        cameraManager.addLog("[$instanceId] $msg")
    }

    fun onEvent(event: VisionUiEvent) {
        Log.d("VisionVM", "[$instanceId] onEvent: ${event::class.java.simpleName}")
        when (event) {
            is VisionUiEvent.CheckPermissions -> checkInitialPermissions(event.context)
            is VisionUiEvent.TriggerCapture -> triggerGlassesCapture()
            is VisionUiEvent.UpdatePermissionStatus -> {
                Log.d("VisionVM", "[$instanceId] Permission updated: ${event.granted}")
                _isPermissionGranted.value = event.granted
            }
            is VisionUiEvent.RunDiagnostic -> {
                cameraManager.runHardwareDiagnostic(event.activity)
            }
            is VisionUiEvent.RunOfficialTest -> {
                runOfficialCameraTest(event.activity)
            }
        }
    }

    private fun observeBridgeCommands() {
        viewModelScope.launch {
            bridgeRepository.glassCommands.collect { cmd ->
                Log.d("VisionVM", "[$instanceId] Command received from bridge: $cmd")
                if (cmd == "CAPTURE_IMAGE") {
                    Log.d("VisionVM", "[$instanceId] Dispatching takePicture to cameraManager")
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
            Log.d("VisionVM", "[$instanceId] Triggering capture. Sending command: CAPTURE_IMAGE")
            cameraManager.addLog("[$instanceId] Sending Remote Command: CAPTURE_IMAGE...")
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
