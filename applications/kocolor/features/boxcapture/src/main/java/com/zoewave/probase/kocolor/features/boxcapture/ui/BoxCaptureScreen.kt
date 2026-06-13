package com.zoewave.probase.kocolor.features.boxcapture.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.BoxCaptureUiState
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureMode
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureStep
import com.zoewave.probase.kocolor.model.CosmeticItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BoxCaptureUiRoute(
    viewModel: BoxCaptureViewModel,
    onSuccess: (CosmeticItem) -> Unit,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is BoxCaptureUiState.Success) {
            onSuccess((uiState as BoxCaptureUiState.Success).item)
        }
    }

    BoxCaptureScreen(
        uiState = uiState,
        onCapture = viewModel::onPhotoCaptured,
        onRetry = viewModel::reset,
        onDismiss = onDismiss
    )
}

@Composable
internal fun BoxCaptureScreen(
    uiState: BoxCaptureUiState,
    onCapture: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is BoxCaptureUiState.Idle -> {
                    CameraView(
                        step = uiState.currentStep,
                        capturedUris = uiState.capturedUris,
                        mode = uiState.mode,
                        onCapture = onCapture,
                        onDismiss = onDismiss
                    )
                }
                is BoxCaptureUiState.Analyzing -> {
                    AnalysisView(progress = uiState.progress)
                }
                is BoxCaptureUiState.Error -> {
                    ErrorView(message = uiState.message, onRetry = onRetry)
                }
                is BoxCaptureUiState.Success -> {
                    // Handled by Route/LaunchedEffect
                }
            }
        }
    }
}

@Composable
private fun CameraView(
    step: CaptureStep,
    capturedUris: List<String>,
    mode: CaptureMode,
    onCapture: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val previewUseCase = remember { Preview.Builder().build() }
    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        previewUseCase.setSurfaceProvider(ContextCompat.getMainExecutor(context)) { request ->
            surfaceRequest = request
        }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                imageCaptureUseCase
            )
        } catch (e: Exception) {
            Log.e("BoxCapture", "Binding failed", e)
        }
    }

    val totalSteps = CaptureStep.getStepsForMode(mode).size

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            val sr = surfaceRequest
            if (sr != null) {
                CameraXViewfinder(surfaceRequest = sr, modifier = Modifier.fillMaxSize())
            }

            // Top Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STEP ${CaptureStep.getStepsForMode(mode).indexOf(step) + 1}/$totalSteps",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = step.label.uppercase(),
                        color = Color(0xFF22d3ee),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0x33000000), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0f172a))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(capturedUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val file = createFile(context)
                        val options = ImageCapture.OutputFileOptions.Builder(file).build()
                        imageCaptureUseCase.takePicture(
                            options,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    onCapture(Uri.fromFile(file).toString())
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("BoxCapture", "Capture failed", exception)
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            val captureLabel = if (mode == CaptureMode.BOX) "CAPTURE BOX" else "CAPTURE PRODUCT"
            Text("TAP TO $captureLabel", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AnalysisView(progress: String) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0f172a)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Color(0xFF22d3ee))
        Spacer(modifier = Modifier.height(24.dp))
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF22d3ee), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("GEMINI ANALYZING", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(progress, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0f172a)).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ANALYSIS FAILED", color = Color(0xFFf472b6), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf472b6))) {
            Text("TRY AGAIN", color = Color.White)
        }
    }
}

private fun createFile(context: Context): File {
    val dir = context.cacheDir
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(dir, name)
}
