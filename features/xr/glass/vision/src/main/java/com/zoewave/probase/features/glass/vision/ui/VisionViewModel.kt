package com.zoewave.probase.features.glass.vision.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.features.glass.vision.data.VisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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
@OptIn(ExperimentalProjectedApi::class)
@HiltViewModel
class VisionViewModel @Inject constructor(
    application: Application,
    private val settings: AiConfigurationSettings,
    private val repository: VisionRepository,
    private val bridgeRepository: GlassBridgeRepository
) : AndroidViewModel(application) {

    private val tag = "VisionVM"
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
    }

    private fun observeBridgeCommands() {
        viewModelScope.launch {
            bridgeRepository.glassCommands.collect { cmd ->
                if (cmd == "CAPTURE_IMAGE") {
                    addLog("Received Remote Command: CAPTURE_IMAGE")
                    if (imageCapture != null) {
                        takePicture()
                    } else {
                        addLog("Cannot execute capture: Camera not bound in this context.")
                    }
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
            addLog("Sending Remote Command: CAPTURE_IMAGE...")
            bridgeRepository.sendGlassCommand("CAPTURE_IMAGE")
        }
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
                Log.e(tag, "Failed to check glasses permission", e)
                _isGlassesPermissionGranted.value = false
            }
        }
    }

    @ExperimentalLensFacing
    @ExperimentalCamera2Interop
    fun setupCamera(activity: Activity) {
        viewModelScope.launch {
            // FIX 1: Run probing in IO thread to prevent ANR/Crash
            withContext(Dispatchers.IO) {
                addLog("Starting Filter-Aware Hardware Probing...")
                
                // Try to create an attribution context for XR hardware tracking
                val baseContext = try {
                    activity.createAttributionContext("xr_projected")
                } catch (e: Exception) {
                    addLog("Warning: Could not create attribution context.")
                    activity
                }

                val providers = listOf(
                    "Glasses" to try { 
                        val ctx = ProjectedContext.createProjectedDeviceContext(baseContext)
                        addLog("PHASE 1 SUCCESS: Created ProjectedContext for Glasses.")
                        ctx
                    } catch (e: Exception) { null },
                    "Host (Phone)" to try { ProjectedContext.createHostDeviceContext(baseContext) } catch (e: Exception) { null },
                    "Application" to getApplication()
                )

                var bound = false
                for ((name, ctx) in providers) {
                    if (ctx == null) continue
                    if (bound) break

                    for (attempt in 1..3) {
                        addLog("Probing $name (Attempt $attempt of 3)...")
                        try {
                            val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                            val ids = cm.cameraIdList
                            addLog("OS reports ${ids.size} cameras in $name: [${ids.joinToString()}]")
                            
                            // 1. Fetch provider for THIS specific context
                            addLog("Fetching CameraProvider for $name...")
                            val cameraProvider = ProcessCameraProvider.awaitInstance(ctx)

                            for (id in ids) {
                                val chars = cm.getCameraCharacteristics(id)
                                val lensFacing = chars.get(CameraCharacteristics.LENS_FACING)
                                val facingName = when (lensFacing) {
                                    CameraCharacteristics.LENS_FACING_FRONT -> "FRONT"
                                    CameraCharacteristics.LENS_FACING_BACK -> "BACK"
                                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "EXTERNAL"
                                    else -> "UNKNOWN"
                                }
                                addLog("-> Found ID $id ($facingName). Testing binding...")

                                // Strategy 1: Strictly separate Glasses ID-binding from standard Fallback selectors
                                if (name != "Glasses") {
                                    // Use standard CameraX selectors for Host/Application fallback to avoid "Filters: 1/2" error
                                    addLog("Using standard $facingName selector for fallback (clearing custom filters)...")
                                    val fallbackSelector = if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                                    
                                    if (bindCameraOnMainThread(activity, cameraProvider, fallbackSelector)) {
                                        _cameraSource.value = "$name ($facingName - Default)"
                                        addLog("SUCCESS: Bound via standard selector for fallback.")
                                        bound = true
                                        break
                                    }
                                } else {
                                    // Strategy 2: For Glasses, use specific ID binding to ensure we hit the projected hardware
                                    addLog("PHASE 4: Hardware Verification for Glasses ID $id ($facingName)...")
                                    val selector = CameraSelector.Builder()
                                        .requireLensFacing(
                                            when (lensFacing) {
                                                CameraCharacteristics.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_FRONT
                                                CameraCharacteristics.LENS_FACING_BACK -> CameraSelector.LENS_FACING_BACK
                                                else -> CameraSelector.LENS_FACING_EXTERNAL
                                            }
                                        )
                                        .addCameraFilter { cameraInfos ->
                                            cameraInfos.filter { info -> 
                                                try { Camera2CameraInfo.from(info).cameraId == id } catch (e: Exception) { false }
                                            }
                                        }
                                        .build()

                                    if (bindCameraOnMainThread(activity, cameraProvider, selector)) {
                                        _cameraSource.value = "$name ($facingName - ID $id)"
                                        addLog("PHASE 6 SUCCESS: Shutter linked to $name (ID $id).")
                                        bound = true
                                        break
                                    }
                                }
                            }

                            if (bound) break
                            addLog("No cameras could be bound in $name. Waiting...")
                            delay(1500.milliseconds)
                        } catch (e: Exception) {
                            addLog("Probe error in $name: ${e.message}")
                            delay(1500.milliseconds)
                        }
                    }
                }

                if (!bound) {
                    addLog("CRITICAL: Failed to bind any camera after deep probing.")
                    _error.value = "Hardware Binding Failed."
                }
            }
        }
    }

    private suspend fun bindCameraOnMainThread(
        activity: Activity, 
        provider: ProcessCameraProvider, 
        selector: CameraSelector
    ): Boolean = withContext(Dispatchers.Main) {
        return@withContext try {
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            provider.unbindAll()
            provider.bindToLifecycle(
                activity as LifecycleOwner,
                selector,
                imageCapture
            )
            true
        } catch (e: Exception) {
            Log.e(tag, "Bind failed for selector: ${e.message}")
            false
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
                Log.e(tag, "Capture failed", exception)
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
        val timestamp = java.text.SimpleDateFormat("HH:ss:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val formattedMsg = "[$timestamp] $message"
        Log.d(tag, formattedMsg)
        _logs.value += formattedMsg
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}
