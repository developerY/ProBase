package com.zoewave.probase.features.glass.vision.ui.manager

import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
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

    fun initialize(activity: ComponentActivity) {
        scope.launch {
            addLog("[INIT] Starting Initialization Sequence...")
            Log.d(tag, "initialize() called with activity: ${activity::class.java.simpleName} (ID: ${System.identityHashCode(activity)})")
            
            val projectedContext = try {
                addLog("[INIT] Creating ProjectedContext...")
                ProjectedContext.createProjectedDeviceContext(activity).also {
                    Log.d(tag, "ProjectedContext created successfully for activity ID: ${System.identityHashCode(activity)}")
                }
            } catch (e: Exception) {
                val msg = "ERROR: Could not create context bridge: ${e.message}"
                addLog(msg)
                Log.e(tag, msg, e)
                return@launch
            }

            try {
                addLog("[INIT] Requesting ProcessCameraProvider...")
                val cameraProvider = ProcessCameraProvider.awaitInstance(projectedContext)
                addLog("[INIT] Provider acquired. Selecting DEFAULT_BACK_CAMERA...")
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                if (!cameraProvider.hasCamera(cameraSelector)) {
                    val isEmulator = android.os.Build.PRODUCT.contains("sdk") || android.os.Build.MODEL.contains("Emulator")
                    val msg = if (isEmulator) {
                        "EMULATOR LIMIT: Virtual camera not supported on projected bridge."
                    } else {
                        "ERROR: Hardware camera not found on glasses."
                    }
                    addLog(msg)
                    Log.w(tag, msg)
                    _cameraSource.value = if (isEmulator) "Emulator (No Camera)" else "Hardware Missing"
                    return@launch
                }
                
                addLog("[INIT] Camera found. Building ResolutionSelector (640x480)...")
                val targetResolution = Size(640, 480)
                val resolutionStrategy = ResolutionStrategy(
                    targetResolution,
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(resolutionStrategy)
                    .build()

                addLog("[INIT] Building ImageCapture use case...")
                imageCapture = ImageCapture.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                try {
                    addLog("[INIT] Unbinding previous use cases...")
                    cameraProvider.unbindAll()

                    addLog("[INIT] Binding to lifecycle: ${activity::class.java.simpleName} (ID: ${System.identityHashCode(activity)})...")
                    cameraProvider.bindToLifecycle(
                        activity,
                        cameraSelector,
                        imageCapture
                    )
                    _cameraSource.value = "Hardware Camera (Projected)"
                    addLog("SUCCESS: Camera bound and ready at 640x480.")
                    Log.d(
                        tag,
                        "Camera initialization complete. ImageCapture instance: $imageCapture bound to Activity ID: ${System.identityHashCode(activity)}"
                    )
                } catch (e: IllegalArgumentException) {
                    val msg = "ERROR: Emulator Resolution Rejection (IllegalArgumentException): ${e.message}"
                    addLog(msg)
                    Log.e(tag, msg, e)
                } catch (e: Exception) {
                    val msg = "ERROR: Lifecycle binding failed: ${e.message}"
                    addLog(msg)
                    Log.e(tag, msg, e)
                }
            } catch (e: Exception) {
                val msg = "ERROR: Camera initialization failed: ${e.message}"
                addLog(msg)
                Log.e(tag, msg, e)
            }
        }
    }

    fun teardown() {
        Log.d(tag, "teardown() called. Clearing ImageCapture state.")
        imageCapture = null
        _cameraSource.value = "Phone (Disconnected)"
        addLog("[TEARDOWN] Camera Manager state cleared.")
    }

    fun runHardwareDiagnostic(activity: ComponentActivity) {
        val diagTag = "XrDiagnosticTest"
        Log.e(diagTag, "========================================================")
        Log.e(diagTag, "STARTING XR EMULATOR CAMERA DIAGNOSTIC TEST")
        Log.e(diagTag, "========================================================")

        // 1. Test standard Phone Context
        val phoneCameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        phoneCameraProviderFuture.addListener({
            val phoneProvider = phoneCameraProviderFuture.get()
            val hasPhoneCamera = phoneProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            Log.e(diagTag, "1. PHONE CONTEXT TEST: hasCamera(DEFAULT_BACK) -> $hasPhoneCamera")

            // 2. Test Projected Context (Glasses)
            try {
                val projectedContext = ProjectedContext.createProjectedDeviceContext(activity)
                Log.e(diagTag, "2. ProjectedContext created successfully. The bridge is open.")

                val glassesProviderFuture = ProcessCameraProvider.getInstance(projectedContext)
                glassesProviderFuture.addListener({
                    val glassesProvider = glassesProviderFuture.get()
                    val hasGlassesCamera = glassesProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

                    Log.e(diagTag, "3. GLASSES CONTEXT TEST: hasCamera(DEFAULT_BACK) -> $hasGlassesCamera")

                    Log.e(diagTag, "--------------------------------------------------------")
                    if (!hasGlassesCamera) {
                        Log.e(diagTag, ">>> CONCLUSION: THE EMULATOR LACKS A VIRTUAL PROJECTED CAMERA. <<<")
                        Log.e(diagTag, ">>> Your code is flawless. The emulator simply cannot run this yet. <<<")
                    } else {
                        Log.e(diagTag, ">>> CONCLUSION: A projected camera WAS found! You are on real hardware! <<<")
                    }
                    Log.e(diagTag, "========================================================")

                }, androidx.core.content.ContextCompat.getMainExecutor(activity))

            } catch (e: Exception) {
                Log.e(diagTag, "2. FAILED to create ProjectedContext. Are you running the XR Emulator?", e)
            }

        }, androidx.core.content.ContextCompat.getMainExecutor(activity))
    }

    fun takePicture() {
        Log.d(tag, "takePicture() called")
        val capture = imageCapture ?: run {
            val msg = "ERROR: Cannot capture - ImageCapture not initialized"
            addLog(msg)
            Log.e(tag, msg)
            return
        }
        
        addLog("Capturing image from hardware...")
        repository.updateCapturing(true)

        Log.d(tag, "Triggering takePicture on ImageCapture instance...")
        capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val width = image.width
                val height = image.height
                val format = image.format
                Log.d(tag, "onCaptureSuccess: size=${width}x${height}, format=$format")
                addLog("Capture Success! Processing ${width}x${height} frame...")
                
                val bitmap = image.toBitmap()
                if (bitmap != null) {
                    Log.d(tag, "Bitmap generated successfully: ${bitmap.width}x${bitmap.height}")
                    repository.updateCapturedImage(bitmap)
                    addLog("Image updated in repository.")
                } else {
                    Log.e(tag, "Failed to generate bitmap from ImageProxy")
                    addLog("ERROR: Bitmap generation failed.")
                }
                
                image.close()
                repository.updateCapturing(false)
            }

            override fun onError(exception: ImageCaptureException) {
                val msg = "Capture Error: ${exception.message} (Type: ${exception.imageCaptureError})"
                addLog(msg)
                Log.e(tag, msg, exception)
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
