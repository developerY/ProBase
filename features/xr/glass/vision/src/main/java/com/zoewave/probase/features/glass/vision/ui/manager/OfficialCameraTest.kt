package com.zoewave.probase.features.glass.vision.ui.manager

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.util.Log
import android.util.Range
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi

private const val TAG = "OfficialCameraTest"

/**
 * Runs the official "Capture an image with the glasses' camera" sample code from the Android XR documentation
 * with extensive high-visibility logging.
 */
@ExperimentalLensFacing
@ExperimentalCamera2Interop
@OptIn(ExperimentalProjectedApi::class)
fun runOfficialCameraTest(activity: ComponentActivity) {
    Log.e(TAG, "========================================================")
    Log.e(TAG, "STARTING OFFICIAL XR SDK CAMERA TEST")
    Log.e(TAG, "========================================================")

    // 1. Get the CameraProvider using the projected context.
    Log.e(TAG, "[STEP 1] Creating ProjectedContext...")
    val projectedContext = try {
        ProjectedContext.createProjectedDeviceContext(activity).also {
            Log.e(TAG, "[SUCCESS] ProjectedContext created.")
        }
    } catch (e: IllegalStateException) {
        Log.e(TAG, "[FAILURE] AI Glasses context could not be created", e)
        return
    }

    Log.e(TAG, "[STEP 2] Requesting ProcessCameraProvider...")
    val cameraProviderFuture = ProcessCameraProvider.getInstance(projectedContext)

    cameraProviderFuture.addListener({
        try {
            Log.e(TAG, "[STEP 3] Acquiring cameraProvider instance...")
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 2. Check for the presence of a camera.
            Log.e(TAG, "[STEP 4] Checking hasCamera(DEFAULT_BACK_CAMERA)...")
            if (!cameraProvider.hasCamera(cameraSelector)) {
                Log.e(TAG, "[FAILURE] The selected camera is not available on this device.")
                return@addListener
            }
            Log.e(TAG, "[SUCCESS] Camera found.")

            // 3. Query supported streaming resolutions using Camera2 Interop.
            Log.e(TAG, "[STEP 5] Querying resolutions via Camera2 Interop...")
            val cameraInfo = cameraProvider.getCameraInfo(cameraSelector)
            val camera2CameraInfo = Camera2CameraInfo.from(cameraInfo)
            val cameraCharacteristics = camera2CameraInfo.getCameraCharacteristic(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )
            Log.e(TAG, "[INFO] SCALER_STREAM_CONFIGURATION_MAP acquired: $cameraCharacteristics")

            // 4. Define the resolution strategy.
            Log.e(TAG, "[STEP 6] Building ResolutionSelector for 1920x1080...")
            val targetResolution = Size(1920, 1080)
            val resolutionStrategy = ResolutionStrategy(
                targetResolution,
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
            )
            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(resolutionStrategy)
                .build()

            // 5. If you have other continuous use cases bound, such as Preview or ImageAnalysis,
            // you can use  Camera2 Interop's CaptureRequestOptions to set the FPS
            Log.e(TAG, "[STEP 7] Building CaptureRequestOptions (30-60 FPS)...")
            val fpsRange = Range(30, 60)
            val captureRequestOptions = CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
                .build()

            // 6. Initialize the ImageCapture use case with options.
            Log.e(TAG, "[STEP 8] Initializing ImageCapture use case...")
            val imageCapture = ImageCapture.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()

            try {
                // Unbind use cases before rebinding.
                Log.e(TAG, "[STEP 9] Unbinding all current use cases...")
                cameraProvider.unbindAll()

                // Bind use cases to camera using the Activity as the LifecycleOwner.
                Log.e(TAG, "[STEP 10] Binding to lifecycle (Activity: ${activity::class.java.simpleName})...")
                cameraProvider.bindToLifecycle(
                    activity,
                    cameraSelector,
                    imageCapture
                )
                Log.e(TAG, "========================================================")
                Log.e(TAG, ">>> SUCCESS: OFFICIAL SDK CAMERA BINDING COMPLETE <<<")
                Log.e(TAG, "========================================================")
            } catch (exc: Exception) {
                Log.e(TAG, "[FAILURE] Use case binding failed", exc)
                Log.e(TAG, "--------------------------------------------------------")
                Log.e(TAG, "Conclusion: The binding failed. Check resolution compatibility.")
                Log.e(TAG, "--------------------------------------------------------")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[FAILURE] Critical error during provider listener execution", e)
        }
    }, ContextCompat.getMainExecutor(activity))
}
