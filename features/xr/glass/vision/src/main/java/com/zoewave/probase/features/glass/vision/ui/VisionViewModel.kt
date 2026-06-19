package com.zoewave.probase.features.glass.vision.ui

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.glass.vision.data.VisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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

@OptIn(ExperimentalProjectedApi::class)
@HiltViewModel
class VisionViewModel @Inject constructor(
    application: Application,
    private val settings: AiConfigurationSettings,
    private val repository: VisionRepository
) : AndroidViewModel(application) {

    private val TAG = "VisionVM"
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null

    private val _cameraSource = MutableStateFlow("Phone")
    private val _isApiKeySet = MutableStateFlow(false)
    private val _isPermissionGranted = MutableStateFlow(false)
    private val _isGlassesPermissionGranted = MutableStateFlow(false)
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VisionUiState> = combine(
        repository.imageDescription,
        repository.isCapturing,
        repository.isAnalyzing,
        _isApiKeySet,
        _cameraSource,
        _isPermissionGranted,
        _isGlassesPermissionGranted,
        repository.capturedImage,
        _logs,
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
            logs = args[8] as List<String>,
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

    fun checkGlassesPermission(activity: Activity) {
        viewModelScope.launch {
            try {
                val projectedContext = ProjectedContext.createProjectedDeviceContext(activity)
                val status = ContextCompat.checkSelfPermission(projectedContext, android.Manifest.permission.CAMERA)
                val granted = status == android.content.pm.PackageManager.PERMISSION_GRANTED
                _isGlassesPermissionGranted.value = granted
                addLog("Glasses camera permission status: ${if (granted) "GRANTED" else "DENIED"}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check glasses permission", e)
                _isGlassesPermissionGranted.value = false
            }
        }
    }

    fun setupCamera(activity: Activity) {
        viewModelScope.launch {
            addLog("Initializing Camera Probing (with retry logic)...")
            
            val providers = listOf(
                "Glasses" to try { ProjectedContext.createProjectedDeviceContext(activity) } catch (e: Exception) { null },
                "Host (Phone)" to try { ProjectedContext.createHostDeviceContext(activity) } catch (e: Exception) { null },
                "Application" to getApplication<Application>()
            )

            var bound = false
            for ((name, ctx) in providers) {
                if (ctx == null) continue
                if (bound) break

                for (attempt in 1..3) {
                    addLog("Probing $name context (Attempt $attempt of 3)...")
                    try {
                        val cameraProvider = ProcessCameraProvider.awaitInstance(ctx)
                        val availableCams = cameraProvider.availableCameraInfos.size
                        addLog("$name reports $availableCams total cameras.")

                        val selectors = listOf(
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        )

                        for (selector in selectors) {
                            if (cameraProvider.hasCamera(selector)) {
                                val lens = if (selector == CameraSelector.DEFAULT_BACK_CAMERA) "BACK" else "FRONT"
                                addLog("Found $lens camera in $name context. Binding...")
                                
                                imageCapture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    activity as LifecycleOwner,
                                    selector,
                                    imageCapture
                                )
                                
                                _cameraSource.value = "$name ($lens)"
                                addLog("SUCCESS: Camera successfully bound to $name.")
                                bound = true
                                break
                            }
                        }
                        
                        if (bound) break
                        
                        addLog("No matching lens found in $name. Waiting before retry...")
                        delay(500)
                    } catch (e: Exception) {
                        addLog("Probe failed for $name: ${e.message}")
                        delay(500)
                    }
                }
            }

            if (!bound) {
                addLog("CRITICAL: No available camera found in any context after retries.")
                _error.value = "Camera binding failed: No cameras found."
            }
        }
    }

    fun takePicture() {
        val capture = imageCapture ?: run {
            addLog("Error: ImageCapture not initialized")
            return
        }
        addLog("Capture Triggered...")
        repository.updateCapturing(true)
        _error.value = null

        capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                addLog("Capture Success! Processing bitmap...")
                val bitmap = image.toBitmap()
                image.close()
                repository.updateCapturedImage(bitmap)
                repository.updateCapturing(false)
                analyzeImage(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                addLog("Capture Error: ${exception.message}")
                Log.e(TAG, "Capture failed", exception)
                repository.updateCapturing(false)
                _error.value = "Capture failed: ${exception.message}"
            }
        })
    }

    private fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            addLog("Starting Gemini Analysis...")
            repository.updateAnalyzing(true)
            try {
                val apiKey = settings.getGeminiApiKey()
                if (apiKey.isNullOrBlank()) {
                    addLog("Error: Gemini API Key missing!")
                    _error.value = "Gemini API Key missing. Check Settings."
                    repository.updateAnalyzing(false)
                    return@launch
                }

                addLog("API Key validated. Model: gemini-1.5-flash")
                // Use gemini-1.5-flash for vision tasks as it's optimized for speed and images
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )

                val prompt = "Describe this image in a few words for someone wearing AI glasses. Be concise."
                addLog("Prompt sent: \"$prompt\"")
                
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)
                val textResponse = response.text ?: "Could not describe image"
                addLog("Gemini Response: $textResponse")
                
                repository.updateImageDescription(textResponse)
                repository.updateAnalyzing(false)
            } catch (e: Exception) {
                addLog("AI Error: ${e.message}")
                _error.value = "AI Error: ${e.message}"
                repository.updateAnalyzing(false)
            }
        }
    }

    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val formattedMsg = "[$timestamp] $message"
        Log.d(TAG, formattedMsg)
        _logs.value = _logs.value + formattedMsg
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer: ByteBuffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}
