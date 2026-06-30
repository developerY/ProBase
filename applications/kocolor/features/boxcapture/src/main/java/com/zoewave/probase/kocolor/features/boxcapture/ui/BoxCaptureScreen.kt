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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
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
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.features.boxcapture.R
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.BoxCaptureUiState
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureMode
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureStep
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

sealed class BoxCaptureEvent {
    data class Capture(val uri: String) : BoxCaptureEvent()
    data class BarcodeScanned(val code: String) : BoxCaptureEvent()
    data object Retry : BoxCaptureEvent()
    data object Dismiss : BoxCaptureEvent()
    data class Success(val item: CosmeticItem) : BoxCaptureEvent()
    data class DeletePhoto(val index: Int) : BoxCaptureEvent()
    data class ChangeMode(val mode: CaptureMode) : BoxCaptureEvent()
    data object SubmitToAi : BoxCaptureEvent()
    data object SkipBarcode : BoxCaptureEvent()
    data object SkipStep : BoxCaptureEvent()
    data class OnColorSelected(val hex: String) : BoxCaptureEvent()
    data object ConfirmColor : BoxCaptureEvent()
    data object ClearColor : BoxCaptureEvent()
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
                            mode = uiState.mode,
                            extractedColorHex = uiState.extractedColorHex
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
                is BoxCaptureUiState.ColorConfirmation -> {
                    ColorConfirmationView(
                        uiState = ColorConfirmationViewUiState(
                            photoUri = uiState.capturedUris.last { it.isNotBlank() },
                            suggestedColors = uiState.suggestedColors,
                            selectedColorHex = uiState.selectedColorHex
                        ),
                        onEvent = onEvent
                    )
                }
                is BoxCaptureUiState.Review -> {
                    ReviewView(
                        uiState = ReviewViewUiState(
                            capturedUris = uiState.capturedUris,
                            barcode = uiState.barcode,
                            ingredientsOcr = uiState.ingredientsOcr,
                            instructionsOcr = uiState.instructionsOcr,
                            enrichmentData = uiState.enrichmentData,
                            manualColorHex = uiState.manualColorHex
                        ),
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
                is BoxCaptureUiState.Error -> {
                    ErrorView(
                        uiState = ErrorViewUiState(uiState.message),
                        onEvent = { onEvent(BoxCaptureEvent.Retry) },
                        navTo = {}
                    )
                }
                is BoxCaptureUiState.Success -> {
                    // Handled by LaunchedEffect in BoxCaptureUiRoute
                }
            }
        }
    }
}

data class CameraViewUiState(
    val step: CaptureStep,
    val capturedUris: List<String>,
    val mode: CaptureMode,
    val extractedColorHex: String? = null
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
    var showColorPicker by remember { mutableStateOf(false) }

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

    val steps = CaptureStep.getStepsForMode(uiState.mode)
    val totalSteps = steps.size
    val stepIndex = steps.indexOf(uiState.step)

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = uiState.extractedColorHex?.let { parseColor(it) } ?: Color.Gray,
            onColorSelected = { 
                onEvent(BoxCaptureEvent.OnColorSelected(it.toHex()))
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Identify Product Color"
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            val sr = surfaceRequest
            if (sr != null) {
                CameraXViewfinder(surfaceRequest = sr, modifier = Modifier.fillMaxSize())
            }

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_boxcapture_step_format, stepIndex + 1, totalSteps),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.step.label.uppercase(),
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

            // Color Picker Overlay for Step 5
            if (uiState.step == CaptureStep.COLOR) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(100.dp)
                        .border(2.dp, Color.White, CircleShape)
                        .padding(4.dp)
                        .background(uiState.extractedColorHex?.let { parseColor(it) } ?: Color.Transparent, CircleShape)
                        .clip(CircleShape)
                )
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
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                itemsIndexed(uiState.capturedUris) { index, uri ->
                    if (uri.isNotBlank()) {
                        Box(modifier = Modifier.size(70.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { onEvent(BoxCaptureEvent.DeletePhoto(index)) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        // Placeholder for skipped steps
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

            if (uiState.step == CaptureStep.BARCODE) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        Icon(Icons.Default.QrCodeScanner, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("START BARCODE SCAN", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { onEvent(BoxCaptureEvent.SkipBarcode) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("NO BARCODE / SKIP", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.step.isSkippable) {
                        TextButton(
                            onClick = { onEvent(BoxCaptureEvent.SkipStep) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(alpha = 0.7f))
                            Spacer(Modifier.width(8.dp))
                            Text("SKIP STEP", color = Color.White.copy(alpha = 0.7f))
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

                    if (uiState.step == CaptureStep.COLOR) {
                        IconButton(
                            onClick = { showColorPicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Palette, null, tint = Color(0xFF22d3ee))
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            val captureLabel = when (uiState.step) {
                CaptureStep.FRONT -> "Capture the product front"
                CaptureStep.BACK -> "Capture the ingredients or info panel"
                CaptureStep.INGREDIENTS -> "Ensure the ingredients list is clear"
                CaptureStep.INSTRUCTIONS -> "Capture usage instructions (if any)"
                CaptureStep.COLOR -> "Capture the best representation of product color"
                CaptureStep.BARCODE -> "Final step: Scan the barcode"
            }
            Text(captureLabel, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

data class ReviewViewUiState(
    val capturedUris: List<String>,
    val barcode: String?,
    val ingredientsOcr: String,
    val instructionsOcr: String,
    val enrichmentData: CosmeticItem? = null,
    val manualColorHex: String? = null
)

@Composable
private fun ReviewView(
    uiState: ReviewViewUiState,
    modifier: Modifier = Modifier,
    onEvent: (BoxCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Capture Review",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onEvent(BoxCaptureEvent.Dismiss) }) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ReviewSection(title = "Captured Photos (${uiState.capturedUris.filter { it.isNotBlank() }.size})") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(uiState.capturedUris) { index, uri ->
                            if (uri.isNotBlank()) {
                                Box {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { onEvent(BoxCaptureEvent.DeletePhoto(index)) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                ReviewSection(title = "Color Identity") {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = uiState.manualColorHex?.let { parseColor(it) } ?: Color.Transparent,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                if (uiState.manualColorHex == null) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color.Gray, modifier = Modifier.padding(8.dp))
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = uiState.manualColorHex ?: "AI will identify color from photos",
                                color = if (uiState.manualColorHex != null) Color.White else Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                ReviewSection(title = "Barcode Intelligence") {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, null, tint = Color(0xFF22d3ee))
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = uiState.barcode ?: "Not scanned",
                                    color = if (uiState.barcode != null) Color.White else Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            uiState.enrichmentData?.let { 
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF22d3ee).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF22d3ee), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Database hit: ${it.brand} ${it.name}",
                                        color = Color(0xFF22d3ee),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                ReviewSection(title = "Local Analysis: Ingredients") {
                    OcrTextArea(text = uiState.ingredientsOcr)
                }
            }

            item {
                ReviewSection(title = "Local Analysis: Instructions") {
                    OcrTextArea(text = uiState.instructionsOcr)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(BoxCaptureEvent.SubmitToAi) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22d3ee))
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text("FINALIZE WITH GEMINI AI", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ReviewSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            color = Color(0xFF22d3ee),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun OcrTextArea(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
    ) {
        val scroll = rememberScrollState()
        Box(modifier = Modifier.padding(16.dp).verticalScroll(scroll)) {
            Text(
                text = if (text.isBlank()) "No text detected locally." else text,
                color = if (text.isBlank()) Color.Gray else Color.White,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
    }
}

data class ColorConfirmationViewUiState(
    val photoUri: String,
    val suggestedColors: List<String>,
    val selectedColorHex: String
)

@Composable
private fun ColorConfirmationView(
    uiState: ColorConfirmationViewUiState,
    onEvent: (BoxCaptureEvent) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = parseColor(uiState.selectedColorHex),
            onColorSelected = { 
                onEvent(BoxCaptureEvent.OnColorSelected(it.toHex())) 
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Refine Product Color"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AI Color Analysis",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select the best shade from the photo",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
        ) {
            AsyncImage(
                model = uiState.photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Selected Color Circle Overlay
            Surface(
                color = parseColor(uiState.selectedColorHex),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
                    .border(4.dp, Color.White, CircleShape),
                shadowElevation = 8.dp
            ) {}
        }

        Spacer(Modifier.height(32.dp))

        // Color Options Palette
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(uiState.suggestedColors) { _, hex ->
                val isSelected = hex == uiState.selectedColorHex
                Surface(
                    onClick = { onEvent(BoxCaptureEvent.OnColorSelected(hex)) },
                    color = parseColor(hex),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(50.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {}
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onEvent(BoxCaptureEvent.ClearColor) },
                modifier = Modifier.weight(1f).height(56.dp),
                border = borderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("CLEAR")
            }

            IconButton(
                onClick = { showColorPicker = true },
                modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.Default.Palette, null, tint = Color(0xFF22d3ee))
            }

            Button(
                onClick = { onEvent(BoxCaptureEvent.ConfirmColor) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22d3ee))
            ) {
                Text("USE COLOR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

data class AnalysisViewUiState(val progress: String)

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

@Composable
private fun CaptureModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF22d3ee) else Color.Transparent,
        shape = CircleShape,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

private fun createFile(context: Context): File {
    val dir = context.cacheDir
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(dir, name)
}
