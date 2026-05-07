package com.zoewave.probase.features.ar.naillab.ui

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@OptIn(ExperimentalMaterial3Api::class)
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

    var handLandmarkerHelper by remember { mutableStateOf<HandLandmarkerHelper?>(null) }

    DisposableEffect(Unit) {
        handLandmarkerHelper = HandLandmarkerHelper(
            context = context,
            handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
                override fun onError(error: String, errorCode: Int) {}
                override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
                    onEvent(NailLabEvent.OnTrackingResult(resultBundle.results.first()))
                }
            }
        )
        onDispose {
            handLandmarkerHelper?.clearHandLandmarker()
            executor.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Nail Lab") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()
                            .also {
                                it.setAnalyzer(executor) { imageProxy ->
                                    val bitmap = imageProxy.toBitmap()
                                    handLandmarkerHelper?.detectLiveStream(bitmap, false)
                                    imageProxy.close()
                                }
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {}
                    }, ContextCompat.getMainExecutor(context))
                }
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
                            height = size.height.toInt()
                        )
                    }
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
