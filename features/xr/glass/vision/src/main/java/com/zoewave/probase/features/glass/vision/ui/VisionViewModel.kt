package com.zoewave.probase.features.xr.glass.vision.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.ExperimentalLensFacing
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VisionUiState(
    val imageDescription: String = "",
    val isCapturing: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isApiKeySet: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalLensFacing::class, ExperimentalCamera2Interop::class, androidx.xr.projected.experimental.ExperimentalProjectedApi::class)
@HiltViewModel
class VisionViewModel @Inject constructor(
    private val repository: VisionRepository,
    private val bridgeRepository: GlassBridgeRepository,
    private val settings: AiConfigurationSettings,
    val cameraManager: SimpleGlassesCameraManager
) : ViewModel() {

    private val instanceId = System.identityHashCode(this)

    private val _isPermissionGranted = MutableStateFlow(false)
    private val _isApiKeySet = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VisionUiState> = combine(
        repository.imageDescription,
        repository.isCapturing,
        repository.isAnalyzing,
        _isApiKeySet,
        _error
    ) { description, isCapturing, isAnalyzing, isApiKeySet, error ->
        VisionUiState(
            imageDescription = description,
            isCapturing = isCapturing,
            isAnalyzing = isAnalyzing,
            isApiKeySet = isApiKeySet,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VisionUiState()
    )

    init {
        Log.d("VisionVM", "[$instanceId] VisionViewModel initialized")
        checkStatus()
        observeIncomingGlassPhotos()
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
                val flowModel = settings.aiModelFlow.firstOrNull()
                val modelName = flowModel?.ifBlank { null } ?: "gemini-3.8-flash"

                cameraManager.addLog("Analyzing image. Model: $modelName")
                val generativeModel = Firebase.ai(
                    backend = GenerativeBackend.googleAI(),
                    useLimitedUseAppCheckTokens = false
                ).generativeModel(
                    modelName = modelName
                )

                val prompt = "Describe this image in a few words for someone wearing AI glasses. Be concise."
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)

                val text = response.text ?: "No description generated"
                cameraManager.addLog("AI Response received: $text")
                repository.updateImageDescription(text)
                repository.updateAnalyzing(false)
            } catch (e: Exception) {
                cameraManager.addLog("AI Error: ${e.message}")
                _error.value = "AI Error: ${e.message}"
                repository.updateAnalyzing(false)
            }
        }
    }

    fun startCamera(activity: ComponentActivity) {
        Log.d("VisionVM", "[$instanceId] Starting camera via SimpleGlassesCameraManager")
        cameraManager.initialize(activity)
    }

    private fun observeIncomingGlassPhotos() {
        viewModelScope.launch {
            Log.d("VisionVM", "[$instanceId] Subscribing to bridgeRepository.latestGlassPhoto")
            repository.capturedImage.collect { bitmap ->
                if (bitmap != null) {
                    Log.d("VisionVM", "[$instanceId] INCOMING GLASS PHOTO RECEIVED! Auto-analyzing...")
                    cameraManager.addLog("[$instanceId] GLASS PHOTO RECEIVED FROM BRIDGE! Analyzing...")
                    repository.updateCapturing(false)
                    analyzeImage(bitmap)
                }
            }
        }
    }

    fun capturePhoto() {
        Log.d("VisionVM", "[$instanceId] capturePhoto() called. Triggering local + remote capture")
        _error.value = null
        repository.updateCapturing(true)

        // 1. Trigger remote Glass
        triggerGlassesCapture()

        // 2. Take local Phone photo
        cameraManager.takePicture()
    }

    fun runCameraDiagnosticTest(activity: ComponentActivity) {
        runOfficialCameraTest(activity)
    }
}
