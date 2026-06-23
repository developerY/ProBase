package com.zoewave.probase.features.glass.vision.ui.manager

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.xr.projected.ProjectedContext
import com.zoewave.probase.features.glass.vision.data.VisionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A simplified camera manager that strictly follows the Android XR SDK documentation
 * for capturing images on projected devices (AI Glasses).
 */
@ExperimentalLensFacing
@ExperimentalCamera2Interop
@androidx.xr.projected.experimental.ExperimentalProjectedApi
@Singleton
class SimpleGlassesCameraManager @Inject constructor(
    private val application: Application,
    private val repository: VisionRepository
) {
    private val tag = "SimpleGlassesCameraManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null

    private val _cameraSource = MutableStateFlow("Phone (Initializing...)")
    val cameraSource: StateFlow<String> = _cameraSource.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _discoveredCameras = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val discoveredCameras: StateFlow<List<Pair<String, String>>> = _discoveredCameras.asStateFlow()

    fun checkGlassesPermission(activity: Activity): Boolean {
        return try {
            val projectedContext = ProjectedContext.createProjectedDeviceContext(activity)
            val status = ContextCompat.checkSelfPermission(projectedContext, android.Manifest.permission.CAMERA)
            val granted = status == PackageManager.PERMISSION_GRANTED
            addLog("Glasses permission check: ${if (granted) "GRANTED" else "DENIED"}")
            granted
        } catch (e: Exception) {
            Log.e(tag, "Failed to check glasses permission", e)
            false
        }
    }

    fun initialize(activity: Activity) {
        scope.launch {
            addLog("Initializing Glasses Camera (Simple Manager)...")
            
            val projectedContext = try {
                ProjectedContext.createProjectedDeviceContext(activity)
            } catch (e: Exception) {
                addLog("ERROR: Could not create ProjectedContext: ${e.message}")
                return@launch
            }

            try {
                val cameraProvider = ProcessCameraProvider.awaitInstance(projectedContext)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                if (!cameraProvider.hasCamera(cameraSelector)) {
                    addLog("ERROR: Glasses camera (DEFAULT_BACK_CAMERA) not found.")
                    return@launch
                }

                // AI Glasses optimized resolution (640x480) to manage thermal/battery
                val targetResolution = Size(640, 480)
                val resolutionStrategy = ResolutionStrategy(
                    targetResolution,
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(resolutionStrategy)
                    .build()

                imageCapture = ImageCapture.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                withContext(Dispatchers.Main) {
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            activity as LifecycleOwner,
                            cameraSelector,
                            imageCapture
                        )
                        _cameraSource.value = "AI Glasses (Projected)"
                        addLog("SUCCESS: Camera bound to AI Glasses context at 640x480.")
                    } catch (e: Exception) {
                        addLog("ERROR: Lifecycle binding failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                addLog("ERROR: Camera initialization failed: ${e.message}")
            }
        }
    }

    fun takePicture() {
        val capture = imageCapture ?: run {
            addLog("ERROR: ImageCapture not initialized")
            return
        }
        
        addLog("Taking picture from glasses...")
        repository.updateCapturing(true)

        capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                addLog("Capture Success!")
                val bitmap = image.toBitmap()
                image.close()
                repository.updateCapturedImage(bitmap)
                repository.updateCapturing(false)
            }

            override fun onError(exception: ImageCaptureException) {
                addLog("Capture Error: ${exception.message}")
                Log.e(tag, "Capture failed", exception)
                repository.updateCapturing(false)
            }
        })
    }

    fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val formattedMsg = "[$timestamp] $message"
        Log.d(tag, formattedMsg)
        _logs.update { it + formattedMsg }
    }
}
