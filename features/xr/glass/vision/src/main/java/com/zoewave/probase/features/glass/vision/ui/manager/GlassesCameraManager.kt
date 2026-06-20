package com.zoewave.probase.features.glass.vision.ui.manager

import android.app.Activity
import android.app.Application
import android.content.Context
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
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null

    private val _cameraSource = MutableStateFlow("Phone")
    val cameraSource: StateFlow<String> = _cameraSource.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun initialize(activity: Activity) {
        scope.launch {
            withContext(Dispatchers.IO) {
                addLog("Starting Filter-Aware Hardware Probing...")
                
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
                    "Application" to application
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

                                if (name != "Glasses") {
                                    addLog("Using standard $facingName selector for fallback...")
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
        val timestamp = java.text.SimpleDateFormat("HH:ss:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val formattedMsg = "[$timestamp] $message"
        Log.d(tag, formattedMsg)
        _logs.update { it + formattedMsg }
    }
}
