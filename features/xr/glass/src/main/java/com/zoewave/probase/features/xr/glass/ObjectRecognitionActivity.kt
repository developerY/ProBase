package com.zoewave.probase.features.xr.glass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.xr.glass.samples.ObjectRecognitionScreen
import androidx.compose.runtime.LaunchedEffect
import java.util.concurrent.Executors

@OptIn(ExperimentalProjectedApi::class)
class ObjectRecognitionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var detectedObject by remember { mutableStateOf<String?>(null) }
            var isVisualUiSupported by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                try {
                    val controller = ProjectedDeviceController.create(this@ObjectRecognitionActivity)
                    isVisualUiSupported = ProjectedCapabilities.hasDisplay(controller)
                } catch (e: Exception) {
                    isVisualUiSupported = false
                }
            }

            GlimmerTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    if (isVisualUiSupported) {
                        ObjectRecognitionScreen(detectedObject = detectedObject)
                    }
                }
            }

            // In a real app, you would check permissions first
            if (isVisualUiSupported && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // For the demo, we use 'this' activity as the context for the camera
                startGlassesObjectDetection(this, this) { label ->
                    detectedObject = label
                }
            }
        }
    }

    private fun startGlassesObjectDetection(
        context: android.content.Context,
        lifecycleOwner: LifecycleOwner,
        onObjectDetected: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 1. Select the default camera on the glasses bridge
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 2. Set up the Image Analysis use case to stream frames
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analyzer ->
                    analyzer.setAnalyzer(executor) { imageProxy: ImageProxy ->
                        // 3. Process the frame (mock AI logic)
                        val detectedLabel = analyzeFrameWithAI(imageProxy)
                        
                        if (detectedLabel != null) {
                            onObjectDetected(detectedLabel)
                        }
                        
                        imageProxy.close()
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, 
                    cameraSelector, 
                    imageAnalyzer
                )
            } catch(e: Exception) {
                Log.e("ObjectRecognition", "Camera binding failed", e)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun analyzeFrameWithAI(imageProxy: ImageProxy): String? {
        // Mocking object detection logic
        // In production, this would use ML Kit or Gemini
        return "Coffee Mug" 
    }
}
