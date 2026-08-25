package com.zoewave.probase.kocolor.features.analyzer.calibration.ui

import android.Manifest
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.zoewave.probase.kocolor.features.analyzer.calibration.ColorExtractionAnalyzer
import com.zoewave.probase.kocolor.model.calibration.ColorProfile
import com.zoewave.probase.kocolor.model.calibration.FacialContrastVector
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun CalibrationCameraScreen(
    viewModel: CalibrationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
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

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (uiState is CalibrationUiState.Success) {
            CalibrationResultContent(
                profile = (uiState as CalibrationUiState.Success).profile,
                onDone = { viewModel.dismissResult() }
            )
        } else {
            if (hasCameraPermission) {
                CameraPreview(
                    imageCapture = imageCapture
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
                        onClick = { 
                            viewModel.startScan()
                            captureAndProcess(
                                imageCapture = imageCapture,
                                executor = cameraExecutor,
                                onResult = viewModel::onScanResult,
                                onError = { viewModel.onError(it) }
                            )
                        },
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
}

private fun captureAndProcess(
    imageCapture: ImageCapture,
    executor: Executor,
    onResult: (FacialContrastVector, Float) -> Unit,
    onError: (String) -> Unit
) {
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val analyzer = ColorExtractionAnalyzer(
                isEnabled = { true },
                onResult = { vector, undertone ->
                    onResult(vector, undertone)
                }
            )
            analyzer.analyze(image)
        }

        override fun onError(exception: ImageCaptureException) {
            onError(exception.message ?: "Capture failed")
        }
    })
}

@Composable
private fun CalibrationResultContent(
    profile: ColorProfile,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PROFILE ESTABLISHED",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = profile.season.name.replace("_", " "),
            style = MaterialTheme.typography.displayMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        
        Spacer(Modifier.height(32.dp))
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                ResultMetricRow(
                    label = "UNDERTONE",
                    value = if (profile.undertone > 0.3f) "WARM" else if (profile.undertone < -0.3f) "COOL" else "NEUTRAL"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))
                ResultMetricRow(
                    label = "CONTRAST DELTA",
                    value = String.format(Locale.US, "%.2f", profile.contrastVector.contrastDelta)
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "OPTIMAL PALETTE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            profile.optimalPaletteHexCodes.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                        .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("FINALIZE PROFILE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
private fun CameraPreview(
    imageCapture: ImageCapture
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
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
