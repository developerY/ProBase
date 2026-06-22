package com.zoewave.probase.features.glass.vision.ui.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.lifecycle.LifecycleOwner
import androidx.xr.projected.ProjectedContext
import com.zoewave.probase.features.glass.vision.data.VisionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalLensFacing
@ExperimentalCamera2Interop
@androidx.xr.projected.experimental.ExperimentalProjectedApi
@Singleton
class GlassesCameraManager @Inject constructor(
    private val application: Application,
    private val repository: VisionRepository
) {
    private val tag = "GlassesCameraManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var initJob: kotlinx.coroutines.Job? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null

    private val _cameraSource = MutableStateFlow("Phone")
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
            addLog("Glasses camera permission status check: ${if (granted) "GRANTED" else "DENIED"}")
            granted
        } catch (e: Exception) {
            Log.e(tag, "Failed to check glasses permission", e)
            false
        }
    }

    fun initialize(activity: Activity) {
        initJob?.cancel()
        initJob = scope.launch {
            withContext(Dispatchers.IO) {
                addLog("Starting Filter-Aware Hardware Probing...")
                _discoveredCameras.value = emptyList()
                
                val baseContext = activity

                val providers = listOf(
                    "Glasses" to try { 
                        ProjectedContext.createProjectedDeviceContext(baseContext)
                    } catch (e: Exception) { 
                        addLog("Glasses Context Error: ${e.message}")
                        null 
                    },
                    "Host (Phone)" to try { 
                        ProjectedContext.createHostDeviceContext(baseContext) 
                    } catch (e: Exception) { null },
                    "Application" to application
                )

                var bound = false
                for ((name, ctx) in providers) {
                    if (ctx == null) continue
                    if (bound) break

                    // 1. Discovery phase (once per provider)
                    try {
                        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                        val ids = cm.cameraIdList
                        addLog("OS reports ${ids.size} cameras in $name context.")
                        
                        for (id in ids) {
                            val chars = cm.getCameraCharacteristics(id)
                            val lensFacing = chars.get(CameraCharacteristics.LENS_FACING)
                            val facingName = when (lensFacing) {
                                CameraCharacteristics.LENS_FACING_FRONT -> "FRONT"
                                CameraCharacteristics.LENS_FACING_BACK -> "BACK"
                                CameraCharacteristics.LENS_FACING_EXTERNAL -> "EXTERNAL"
                                else -> "UNKNOWN"
                            }
                            val entry = name to "ID $id ($facingName)"
                            if (!_discoveredCameras.value.contains(entry)) {
                                _discoveredCameras.update { it + entry }
                            }
                        }
                    } catch (e: Exception) {
                        addLog("Discovery error in $name: ${e.message}")
                    }

                    // 2. Probing phase
                    for (attempt in 1..3) {
                        addLog("Probing $name (Attempt $attempt of 3)...")
                        try {
                            val cameraProvider = ProcessCameraProvider.awaitInstance(ctx)

                            // Try DEFAULT_BACK_CAMERA directly if no IDs were found (Glasses fallback)
                            if (_discoveredCameras.value.none { it.first == name } && name == "Glasses") {
                                addLog("No IDs found for Glasses. Attempting DEFAULT_BACK_CAMERA fallback...")
                                if (bindCameraOnMainThread(activity, cameraProvider, CameraSelector.DEFAULT_BACK_CAMERA)) {
                                    _cameraSource.value = "Glasses (Fallback Selector)"
                                    addLog("SUCCESS: Bound via DEFAULT_BACK_CAMERA fallback.")
                                    bound = true
                                    break
                                }
                            }

                            val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                            val ids = cm.cameraIdList

                            for (id in ids) {
                                val chars = cm.getCameraCharacteristics(id)
                                val lensFacing = chars.get(CameraCharacteristics.LENS_FACING)
                                val facingName = when (lensFacing) {
                                    CameraCharacteristics.LENS_FACING_FRONT -> "FRONT"
                                    CameraCharacteristics.LENS_FACING_BACK -> "BACK"
                                    else -> "EXTERNAL"
                                }
                                
                                addLog("-> Testing binding for ID $id ($facingName)...")

                                if (name != "Glasses") {
                                    val fallbackSelector = if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                                    
                                    if (bindCameraOnMainThread(activity, cameraProvider, fallbackSelector)) {
                                        _cameraSource.value = "$name ($facingName - Default)"
                                        addLog("SUCCESS: Bound via standard selector.")
                                        bound = true
                                        break
                                    }
                                } else {
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
                                        _cameraSource.value = "Glasses (ID $id)"
                                        addLog("SUCCESS: Bound Glasses ID $id.")
                                        bound = true
                                        break
                                    }
                                }
                            }

                            if (bound) break
                            delay(1000.milliseconds)
                        } catch (e: Exception) {
                            addLog("Probe error in $name: ${e.message}")
                            delay(1000.milliseconds)
                        }
                    }
                }

                if (!bound) {
                    addLog("CRITICAL: Failed to bind any camera.")
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
            val errorMsg = "Bind failed: ${e.message}"
            Log.e(tag, errorMsg)
            addLog(errorMsg)
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

        capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                addLog("Capture Success! Processing bitmap...")
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
