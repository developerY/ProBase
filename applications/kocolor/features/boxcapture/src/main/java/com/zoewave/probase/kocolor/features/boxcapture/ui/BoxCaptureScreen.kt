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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.probase.kocolor.features.boxcapture.R
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.BoxCaptureUiState
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureMode
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureStep
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

sealed class BoxCaptureEvent {
    data class Capture(val uri: String) : BoxCaptureEvent()
    data class BarcodeScanned(val code: String) : BoxCaptureEvent()
    data class UpdateDraft(val item: CosmeticItem) : BoxCaptureEvent()
    data object ConfirmSave : BoxCaptureEvent()
    data object Retry : BoxCaptureEvent()
    data object Dismiss : BoxCaptureEvent()
    data class Success(val item: CosmeticItem) : BoxCaptureEvent()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BoxCaptureUiRoute(
    uiState: BoxCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (BoxCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    var hasRequestedPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
            hasRequestedPermission = true
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is BoxCaptureUiState.Success) {
            onEvent(BoxCaptureEvent.Success(uiState.item))
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            BoxCaptureScreen(
                uiState = uiState,
                modifier = modifier,
                onEvent = onEvent,
                navTo = navTo
            )
        }
        hasRequestedPermission && !cameraPermissionState.status.isGranted -> {
            PermissionDeniedView(
                uiState = Unit,
                modifier = modifier,
                onEvent = { onEvent(BoxCaptureEvent.Dismiss) },
                navTo = {}
            )
        }
        else -> {
            Box(modifier.fillMaxSize().background(Color(0xFF0f172a)))
        }
    }
}

@Composable
private fun PermissionDeniedView(
    uiState: Unit,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFFf472b6),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.applications_kocolor_features_boxcapture_permission_required),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.applications_kocolor_features_boxcapture_permission_desc),
            color = Color.White.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onEvent(Unit) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(stringResource(R.string.applications_kocolor_features_boxcapture_go_back), color = Color.Black)
        }
    }
}

@Composable
internal fun BoxCaptureScreen(
    uiState: BoxCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (BoxCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is BoxCaptureUiState.Idle -> {
                    CameraView(
                        uiState = CameraViewUiState(
                            step = uiState.currentStep,
                            capturedUris = uiState.capturedUris,
                            mode = uiState.mode
                        ),
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
                is BoxCaptureUiState.Analyzing -> {
                    AnalysisView(
                        uiState = AnalysisViewUiState(uiState.progress),
                        onEvent = {},
                        navTo = {}
                    )
                }
                is BoxCaptureUiState.Error -> {
                    ErrorView(
                        uiState = ErrorViewUiState(uiState.message),
                        onEvent = { onEvent(BoxCaptureEvent.Retry) },
                        navTo = {}
                    )
                }
                is BoxCaptureUiState.Reviewing -> {
                    ReviewView(
                        item = uiState.item,
                        onEvent = onEvent
                    )
                }
                is BoxCaptureUiState.Success -> {
                }
            }
        }
    }
}

data class CameraViewUiState(
    val step: CaptureStep,
    val capturedUris: List<String>,
    val mode: CaptureMode
)

@Composable
private fun CameraView(
    uiState: CameraViewUiState,
    modifier: Modifier = Modifier,
    onEvent: (BoxCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    LaunchedEffect(uiState.step) {
        if (uiState.step == CaptureStep.BARCODE) {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    barcode.rawValue?.let { onEvent(BoxCaptureEvent.BarcodeScanned(it)) }
                }
                .addOnFailureListener {
                    // Fail silently or let user retry
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
            Log.e("BoxCapture", "Binding failed", e)
        }
    }

    val totalSteps = CaptureStep.getStepsForMode(uiState.mode).size

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            val sr = surfaceRequest
            if (sr != null) {
                CameraXViewfinder(surfaceRequest = sr, modifier = Modifier.fillMaxSize())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_boxcapture_step_format, CaptureStep.getStepsForMode(uiState.mode).indexOf(uiState.step) + 1, totalSteps),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.step.getLabel(uiState.mode).uppercase(),
                        color = Color(0xFF22d3ee),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = { onEvent(BoxCaptureEvent.Dismiss) },
                    modifier = Modifier.background(Color(0x33000000), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.applications_kocolor_features_boxcapture_close), tint = Color.White)
                }
            }
        }

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
                items(uiState.capturedUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.step == CaptureStep.BARCODE) {
                Button(
                    onClick = {
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                barcode.rawValue?.let { onEvent(BoxCaptureEvent.BarcodeScanned(it)) }
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22d3ee))
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("START BARCODE SCAN", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
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
                                        onEvent(BoxCaptureEvent.Capture(Uri.fromFile(file).toString()))
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
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            val captureLabel = if (uiState.mode == CaptureMode.BOX) stringResource(R.string.applications_kocolor_features_boxcapture_tap_to_capture_box) else stringResource(R.string.applications_kocolor_features_boxcapture_tap_to_capture_product)
            Text(captureLabel, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

data class AnalysisViewUiState(val progress: String)

@Composable
private fun ReviewView(
    item: CosmeticItem,
    onEvent: (BoxCaptureEvent) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a))
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "REVIEW PRODUCT",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Front Image
        Surface(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        ReviewTextField(
            label = "Product Name",
            value = item.name,
            onValueChange = { onEvent(BoxCaptureEvent.UpdateDraft(item.copy(name = it))) }
        )

        ReviewTextField(
            label = "Brand",
            value = item.brand,
            onValueChange = { onEvent(BoxCaptureEvent.UpdateDraft(item.copy(brand = it))) }
        )

        ReviewTextField(
            label = "Barcode / Batch Code",
            value = item.batchCode ?: "",
            onValueChange = { onEvent(BoxCaptureEvent.UpdateDraft(item.copy(batchCode = it))) }
        )

        ReviewTextField(
            label = "Ingredients (Auto-Extracted)",
            value = item.instructions ?: "", // Instructions used for ingredients text
            onValueChange = { onEvent(BoxCaptureEvent.UpdateDraft(item.copy(instructions = it))) },
            singleLine = false,
            minLines = 5
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onEvent(BoxCaptureEvent.ConfirmSave) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22d3ee))
        ) {
            Text("CONFIRM & SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        
        TextButton(onClick = { onEvent(BoxCaptureEvent.Dismiss) }) {
            Text("Discard", color = Color.White.copy(alpha = 0.6f))
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ReviewTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF22d3ee), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF22d3ee),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color(0xFF22d3ee)
            )
        )
    }
}

@Composable
private fun AnalysisView(
    uiState: AnalysisViewUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val isLocal = uiState.progress.contains("Local")
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF0f172a)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = if (isLocal) Color(0xFF4ade80) else Color(0xFF22d3ee))
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Default.AutoAwesome, 
            contentDescription = null, 
            tint = if (isLocal) Color(0xFF4ade80) else Color(0xFF22d3ee), 
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isLocal) stringResource(R.string.applications_kocolor_features_boxcapture_local_analyzing) else stringResource(R.string.applications_kocolor_features_boxcapture_gemini_analyzing), 
            color = Color.White, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold
        )
        Text(uiState.progress, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        
        if (isLocal) {
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                color = Color(0x1A4ADE80),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.applications_kocolor_features_boxcapture_offline_mode_desc),
                    color = Color(0xFF4ade80),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

data class ErrorViewUiState(val message: String)

@Composable
private fun ErrorView(
    uiState: ErrorViewUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF0f172a)).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.applications_kocolor_features_boxcapture_analysis_failed), color = Color(0xFFf472b6), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(uiState.message, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onEvent, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf472b6))) {
            Text(stringResource(R.string.applications_kocolor_features_boxcapture_try_again), color = Color.White)
        }
    }
}

private fun createFile(context: Context): File {
    val dir = context.cacheDir
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(dir, name)
}
