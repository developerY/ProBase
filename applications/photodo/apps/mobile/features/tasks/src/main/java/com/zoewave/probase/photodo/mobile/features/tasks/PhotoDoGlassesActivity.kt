package com.zoewave.probase.photodo.mobile.features.tasks

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.xr.glimmer.GlimmerTheme
import com.zoewave.probase.photodo.mobile.features.tasks.data.PhotoDoLiveSessionManager
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.ProjectedTaskDetailScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhotoDoGlassesActivity : ComponentActivity() {

    @Inject
    lateinit var liveSessionManager: PhotoDoLiveSessionManager
    
    private val viewModel: TaskDetailViewModel by viewModels()
    private var imageCapture: ImageCapture? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectId = intent.getLongExtra("projectId", -1L)
        if (projectId != -1L) {
            viewModel.loadTaskDetails(projectId)
            liveSessionManager.setProjectId(projectId)
            lifecycle.addObserver(liveSessionManager)
            
            // Auto-start Gemini Live when the projected activity begins
            liveSessionManager.startConversation()
            
            // Setup Camera for image capture
            setupCamera()
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val isAiActive by liveSessionManager.isSessionActive.collectAsState()
            var isCapturing by remember { mutableStateOf(false) }
            
            // Link local capturing state to tool calls
            LaunchedEffect(Unit) {
                liveSessionManager.captureRequests.collect {
                    isCapturing = true
                    takePicture {
                        isCapturing = false
                    }
                }
            }

            GlimmerTheme {
                ProjectedTaskDetailScreen(
                    uiState = uiState,
                    isAiActive = isAiActive,
                    aiAudioLevel = { 0f },
                    isCapturing = isCapturing,
                    onToggleTask = { taskId, isChecked ->
                        // Simplified toggle for projected view
                    }
                )
            }
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture)
            } catch (e: Exception) {
                Log.e("PhotoDoGlasses", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture(onComplete: () -> Unit) {
        val capture = imageCapture ?: run {
            onComplete()
            return
        }
        
        capture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    
                    Log.d("PhotoDoGlasses", "Multimodal: Image captured successfully")
                    // In a real app, save bitmap and notify viewModel
                    onComplete()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("PhotoDoGlasses", "Capture failed", exception)
                    onComplete()
                }
            }
        )
    }
}
