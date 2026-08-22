package com.zoewave.probase.kocolor.features.analyzer.calibration.ui

import android.Manifest
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.zoewave.probase.kocolor.features.analyzer.calibration.ColorExtractionAnalyzer
import com.zoewave.probase.kocolor.model.calibration.FacialContrastVector
import java.util.concurrent.Executors

@Composable
fun CalibrationCameraScreen(
    viewModel: CalibrationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val lightingStatus by viewModel.lightingStatus.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                CalibrationEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            CameraPreview(
                onScanResult = viewModel::onScanResult,
                isScanning = uiState is CalibrationUiState.Scanning
            )
            
            CalibrationOverlay()
            
            LightingFeedbackPill(
                status = lightingStatus,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ALIGN YOUR FACE",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    color = Color.White
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.startScan() },
                    enabled = uiState !is CalibrationUiState.Scanning,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, 
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White.copy(alpha = 0.3f),
                        disabledContentColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.height(56.dp).padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = if (uiState is CalibrationUiState.Scanning) "SCANNING..." else "SCAN PROFILE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onScanResult: (FacialContrastVector, Float) -> Unit,
    isScanning: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ColorExtractionAnalyzer { vector, undertone ->
                        if (isScanning) {
                            onScanResult(vector, undertone)
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                // Log error
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@Composable
private fun CalibrationOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 2.dp.toPx()
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        
        // Dark translucent overlay
        drawRect(color = Color.Black.copy(alpha = 0.6f))
        
        // Face reticle (clear area)
        val reticleWidth = size.width * 0.7f
        val reticleHeight = size.height * 0.5f
        val left = (size.width - reticleWidth) / 2
        val top = (size.height - reticleHeight) / 3
        
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(reticleWidth, reticleHeight),
            cornerRadius = CornerRadius(24.dp.toPx()),
            blendMode = BlendMode.Clear
        )
        
        // Dashed border
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(reticleWidth, reticleHeight),
            cornerRadius = CornerRadius(24.dp.toPx()),
            style = Stroke(width = strokeWidth, pathEffect = dashEffect)
        )
    }
}

@Composable
private fun LightingFeedbackPill(
    status: LightingStatus,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (status) {
        LightingStatus.Optimal -> "Lighting Optimal" to Color(0xFF4CAF50)
        LightingStatus.Poor -> "Move to a window for natural light" to Color(0xFFFFC107)
        else -> "Detecting lighting..." to Color.Gray
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(50)),
        color = Color.Black.copy(alpha = 0.8f),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
