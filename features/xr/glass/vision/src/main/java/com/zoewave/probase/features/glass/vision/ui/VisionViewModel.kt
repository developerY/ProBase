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
import androidx.core.content.ContextCompat
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
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VisionUiState> = combine(
        repository.imageDescription,
        repository.isCapturing,
        repository.isAnalyzing,
        _isApiKeySet,
        _cameraSource,
        _isPermissionGranted,
        _error
    ) { args: Array<Any?> ->
        VisionUiState(
            imageDescription = args[0] as String,
            isCapturing = args[1] as Boolean,
            isAnalyzing = args[2] as Boolean,
            isApiKeySet = args[3] as Boolean,
            cameraSource = args[4] as String,
            isPermissionGranted = args[5] as Boolean,
            error = args[6] as String?
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

    fun setupCamera(activity: Activity) {
        // Try to use Glasses context to target glasses hardware camera
        val (finalContext, source) = try {
            ProjectedContext.createProjectedDeviceContext(activity) to "Glasses"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create projected context: ${e.message}. Falling back to Phone.")
            activity to "Phone"
        }

        _cameraSource.value = source

        val cameraProviderFuture = ProcessCameraProvider.getInstance(finalContext)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                
                // Select the camera. When using the projected context, DEFAULT_BACK_CAMERA maps to the AI glasses' camera.
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    activity as LifecycleOwner,
                    cameraSelector,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
                _error.value = "Camera Init Failed: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(finalContext))
    }

    fun takePicture() {
        val capture = imageCapture ?: return
        repository.updateCapturing(true)
        _error.value = null

        capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                image.close()
                repository.updateCapturedImage(bitmap)
                repository.updateCapturing(false)
                analyzeImage(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Capture failed", exception)
                repository.updateCapturing(false)
                _error.value = "Capture failed: ${exception.message}"
            }
        })
    }

    private fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            repository.updateAnalyzing(true)
            try {
                val apiKey = settings.getGeminiApiKey()
                if (apiKey.isNullOrBlank()) {
                    _error.value = "Gemini API Key missing. Check Settings."
                    repository.updateAnalyzing(false)
                    return@launch
                }

                // Use gemini-1.5-flash for vision tasks as it's optimized for speed and images
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
                repository.updateImageDescription(response.text ?: "Could not describe image")
                repository.updateAnalyzing(false)
            } catch (e: Exception) {
                _error.value = "AI Error: ${e.message}"
                repository.updateAnalyzing(false)
            }
        }
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
