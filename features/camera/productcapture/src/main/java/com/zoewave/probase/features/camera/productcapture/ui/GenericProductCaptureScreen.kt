package com.zoewave.probase.features.camera.productcapture.ui

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GenericProductCaptureUiRoute(
    config: ProductCaptureSessionConfig,
    capturedUris: List<String>,
    currentStepIndex: Int,
    onEvent: (ProductCaptureUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        GenericProductCaptureScreen(
            config = config,
            capturedUris = capturedUris,
            currentStepIndex = currentStepIndex,
            onEvent = onEvent,
            modifier = modifier
        )
    } else {
        Box(modifier.fillMaxSize().background(Color(0xFF0f172a)))
    }
}

@Composable
internal fun GenericProductCaptureScreen(
    config: ProductCaptureSessionConfig,
    capturedUris: List<String>,
    currentStepIndex: Int,
    onEvent: (ProductCaptureUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    val currentStep = config.steps.getOrNull(currentStepIndex) ?: return

    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    LaunchedEffect(currentStepIndex) {
        if (currentStep.isBarcodeStep) {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    barcode.rawValue?.let { onEvent(ProductCaptureUiEvent.BarcodeScanned(it)) }
                }
                .addOnFailureListener {
                    // Handle failure or cancellation if needed
                }
        }
    }

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
            Log.e("GenericCapture", "Binding failed", e)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { padding ->
        if (currentStep.isBarcodeStep) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFF0f172a)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    null,
                    tint = config.themeColor,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Launching Barcode Scanner...",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = {
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                barcode.rawValue?.let { onEvent(ProductCaptureUiEvent.BarcodeScanned(it)) }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = config.themeColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth().height(56.dp)
                ) {
                    Text("RETRY SCAN", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { onEvent(ProductCaptureUiEvent.Close) }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.6f))
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    val sr = surfaceRequest
                    if (sr != null) {
                        CameraXViewfinder(surfaceRequest = sr, modifier = Modifier.fillMaxSize())
                    }

                    // Step Overlay
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STEP ${currentStepIndex + 1} OF ${config.steps.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentStep.label.uppercase(),
                                color = config.themeColor,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { onEvent(ProductCaptureUiEvent.Close) },
                            modifier = Modifier.background(Color(0x33000000), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Custom Step Viewfinder Overlay
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (currentStep.id.contains("PRICE", ignoreCase = true)) {
                            PriceScanningOverlay(config.themeColor)
                        } else {
                            currentStep.viewfinderOverlay()
                        }
                    }
                }

                // Bottom Gallery & Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0f172a))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        itemsIndexed(capturedUris) { index, uri ->
                            if (uri.isNotBlank()) {
                                Box(modifier = Modifier.size(70.dp)) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { onEvent(ProductCaptureUiEvent.DeletePhoto(index)) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Skipped", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep.isSkippable) {
                            TextButton(
                                onClick = { onEvent(ProductCaptureUiEvent.SkipStep) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(alpha = 0.7f))
                                Spacer(Modifier.width(8.dp))
                                Text("SKIP", color = Color.White.copy(alpha = 0.7f))
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }

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
                                                onEvent(ProductCaptureUiEvent.Capture(Uri.fromFile(file).toString()))
                                            }
                                            override fun onError(exception: ImageCaptureException) {
                                                Log.e("GenericCapture", "Capture failed", exception)
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
                            Icon(Icons.Default.CameraAlt, null, tint = Color.Black, modifier = Modifier.size(32.dp))
                        }

                        Spacer(Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(currentStep.hint, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun PriceScanningOverlay(themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "ALIGN PRICE WITHIN FRAME",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(120.dp)
                .border(2.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
        )
    }
}

private fun createFile(context: Context): File {
    val dir = context.cacheDir
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(dir, name)
}
