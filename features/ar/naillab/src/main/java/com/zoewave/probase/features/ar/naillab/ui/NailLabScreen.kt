package com.zoewave.probase.features.ar.naillab.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.zoewave.probase.features.ar.naillab.data.HandLandmarkerHelper
import com.zoewave.probase.features.ar.naillab.domain.NailRenderingEngine
import java.util.concurrent.Executors

@Composable
fun NailLabUiRoute(
    colorHex: String,
    finish: String,
    onBack: () -> Unit,
    viewModel: NailLabViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(colorHex, finish) {
        viewModel.onEvent(NailLabEvent.OnColorChanged(colorHex))
        viewModel.onEvent(NailLabEvent.OnFinishChanged(finish))
    }

    NailLabScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NailLabScreen(
    uiState: NailLabUiState,
    onEvent: (NailLabEvent) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val renderingEngine = remember { NailRenderingEngine() }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Camera Permission State
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val handLandmarkerHelper = remember(uiState.isFrontCamera) {
        HandLandmarkerHelper(
            context = context,
            handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
                override fun onError(error: String, errorCode: Int) {
                    errorMessage = error
                }
                override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
                    if (resultBundle.results.isNotEmpty()) {
                        onEvent(NailLabEvent.OnTrackingResult(resultBundle.results.first()))
                    }
                }
            }
        )
    }

    DisposableEffect(handLandmarkerHelper) {
        onDispose {
            handLandmarkerHelper.clearHandLandmarker()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(cameraPermissionState.status.isGranted, uiState.isFrontCamera, handLandmarkerHelper) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
            return@LaunchedEffect
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor) { imageProxy ->
                        val bitmap = try {
                            imageProxy.toBitmap()
                        } catch (e: Exception) {
                            null
                        }
                        if (bitmap != null) {
                            handLandmarkerHelper.detectLiveStream(
                                bitmap = bitmap,
                                isFrontCamera = uiState.isFrontCamera,
                                rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            )
                        }
                        imageProxy.close()
                    }
                }

            val cameraSelector = if (uiState.isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                errorMessage = "Camera binding failed: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Nail Lab") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(NailLabEvent.OnToggleCamera) }) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (cameraPermissionState.status.isGranted) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // AR Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val result = uiState.latestResult
                    if (result != null) {
                        drawContext.canvas.nativeCanvas.apply {
                            renderingEngine.drawNails(
                                canvas = this,
                                result = result,
                                colorHex = uiState.colorHex,
                                finish = uiState.finish,
                                width = size.width.toInt(),
                                height = size.height.toInt(),
                                isMirrored = uiState.isFrontCamera
                            )
                        }
                    }
                }
            } else {
                // Permission Denied State
                val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                    "Camera permission is needed for AR nail simulation."
                } else {
                    "Camera permission denied. Please enable it in settings."
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = textToShow,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (cameraPermissionState.status.shouldShowRationale) {
                            cameraPermissionState.launchPermissionRequest()
                        } else {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    }) {
                        Text(if (cameraPermissionState.status.shouldShowRationale) "Grant Permission" else "Open Settings")
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Info badge
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Wearing: ${uiState.colorHex} (${uiState.finish})",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
