package com.zoewave.probase.kocolor.features.clothingcapture.ui

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
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.features.clothingcapture.R
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureStep
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

sealed class ClothingCaptureEvent {
    data class Capture(val uri: String) : ClothingCaptureEvent()
    data object Retry : ClothingCaptureEvent()
    data object Dismiss : ClothingCaptureEvent()
    data class Success(val item: ClothingItem) : ClothingCaptureEvent()
    data class DeletePhoto(val index: Int) : ClothingCaptureEvent()
    data object SubmitToAi : ClothingCaptureEvent()
    data object SkipStep : ClothingCaptureEvent()
    data class OnColorSelected(val hex: String) : ClothingCaptureEvent()
    data object ConfirmColor : ClothingCaptureEvent()
    data object ClearColor : ClothingCaptureEvent()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ClothingCaptureUiRoute(
    uiState: ClothingCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (ClothingCaptureEvent) -> Unit,
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
        if (uiState is ClothingCaptureUiState.Success) {
            onEvent(ClothingCaptureEvent.Success(uiState.item))
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            ClothingCaptureScreen(
                uiState = uiState,
                modifier = modifier,
                onEvent = onEvent,
                navTo = navTo
            )
        }
        hasRequestedPermission && !cameraPermissionState.status.isGranted -> {
            Box(modifier.fillMaxSize().background(Color(0xFF0f172a)).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Camera permission required", color = Color.White)
                    Button(onClick = { onEvent(ClothingCaptureEvent.Dismiss) }) {
                        Text("Go Back")
                    }
                }
            }
        }
        else -> {
            Box(modifier.fillMaxSize().background(Color(0xFF0f172a)))
        }
    }
}

@Composable
internal fun ClothingCaptureScreen(
    uiState: ClothingCaptureUiState,
    modifier: Modifier = Modifier,
    onEvent: (ClothingCaptureEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is ClothingCaptureUiState.Idle -> {
                    CameraView(
                        uiState = CameraViewUiState(
                            step = uiState.currentStep,
                            capturedUris = uiState.capturedUris,
                            extractedColorHex = uiState.extractedColorHex
                        ),
                        onEvent = onEvent
                    )
                }
                is ClothingCaptureUiState.Analyzing -> {
                    AnalysisView(uiState.progress)
                }
                is ClothingCaptureUiState.ColorConfirmation -> {
                    ColorConfirmationView(
                        uiState = ColorConfirmationViewUiState(
                            photoUri = uiState.capturedUris.last { it.isNotBlank() },
                            suggestedColors = uiState.suggestedColors,
                            selectedColorHex = uiState.selectedColorHex
                        ),
                        onEvent = onEvent
                    )
                }
                is ClothingCaptureUiState.Review -> {
                    ReviewView(
                        uiState = ReviewViewUiState(
                            capturedUris = uiState.capturedUris,
                            labelsOcr = uiState.labelsOcr,
                            manualColorHex = uiState.manualColorHex
                        ),
                        onEvent = onEvent
                    )
                }
                is ClothingCaptureUiState.Error -> {
                    ErrorView(uiState.message, onRetry = { onEvent(ClothingCaptureEvent.Retry) })
                }
                is ClothingCaptureUiState.Success -> {}
            }
        }
    }
}

data class CameraViewUiState(
    val step: ClothingCaptureStep,
    val capturedUris: List<String>,
    val extractedColorHex: String? = null
)

@Composable
private fun CameraView(
    uiState: CameraViewUiState,
    onEvent: (ClothingCaptureEvent) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var showColorPicker by remember { mutableStateOf(false) }

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
            Log.e("ClothingCapture", "Binding failed", e)
        }
    }

    val steps = ClothingCaptureStep.ALL
    val totalSteps = steps.size
    val stepIndex = steps.indexOf(uiState.step)

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = uiState.extractedColorHex?.let { parseColor(it) } ?: Color.Gray,
            onColorSelected = { 
                onEvent(ClothingCaptureEvent.OnColorSelected(it.toHex()))
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Identify Fabric Color"
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                        text = "STEP ${stepIndex + 1} OF $totalSteps",
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
                    onClick = { onEvent(ClothingCaptureEvent.Dismiss) },
                    modifier = Modifier.background(Color(0x33000000), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            if (uiState.step == ClothingCaptureStep.COLOR) {
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
                                onClick = { onEvent(ClothingCaptureEvent.DeletePhoto(index)) },
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
                if (uiState.step.isSkippable) {
                    TextButton(
                        onClick = { onEvent(ClothingCaptureEvent.SkipStep) },
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
                                        onEvent(ClothingCaptureEvent.Capture(Uri.fromFile(file).toString()))
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("ClothingCapture", "Capture failed", exception)
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

                if (uiState.step == ClothingCaptureStep.COLOR) {
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
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Capture clearly for the best results", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
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
    onEvent: (ClothingCaptureEvent) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = parseColor(uiState.selectedColorHex),
            onColorSelected = { 
                onEvent(ClothingCaptureEvent.OnColorSelected(it.toHex())) 
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Refine Item Color"
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
            text = "Fabric Color Analysis",
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(uiState.suggestedColors) { _, hex ->
                val isSelected = hex == uiState.selectedColorHex
                Surface(
                    onClick = { onEvent(ClothingCaptureEvent.OnColorSelected(hex)) },
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
                onClick = { onEvent(ClothingCaptureEvent.ClearColor) },
                modifier = Modifier.weight(1f).height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
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
                onClick = { onEvent(ClothingCaptureEvent.ConfirmColor) },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22d3ee))
            ) {
                Text("USE COLOR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class ReviewViewUiState(
    val capturedUris: List<String>,
    val labelsOcr: String,
    val manualColorHex: String? = null
)

@Composable
private fun ReviewView(
    uiState: ReviewViewUiState,
    onEvent: (ClothingCaptureEvent) -> Unit
) {
    Column(
        modifier = Modifier
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
                text = "Wardrobe Review",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onEvent(ClothingCaptureEvent.Dismiss) }) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text("CAPTURED PHOTOS", color = Color(0xFF22d3ee), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
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
                                    onClick = { onEvent(ClothingCaptureEvent.DeletePhoto(index)) },
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

            item {
                Text("FABRIC COLOR", color = Color(0xFF22d3ee), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
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

            item {
                Text("LOCAL LABEL OCR", color = Color(0xFF22d3ee), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
                ) {
                    val scroll = rememberScrollState()
                    Box(modifier = Modifier.padding(16.dp).verticalScroll(scroll)) {
                        Text(
                            text = if (uiState.labelsOcr.isBlank()) "No label text detected." else uiState.labelsOcr,
                            color = if (uiState.labelsOcr.isBlank()) Color.Gray else Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(ClothingCaptureEvent.SubmitToAi) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22d3ee))
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text("FINALIZE WITH GEMINI AI", color = Color.Black, fontWeight = FontWeight.Bold)
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
        Text(progress, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0f172a)).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Extraction Failed", color = Color.Red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

private fun createFile(context: Context): File {
    val dir = context.cacheDir
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(dir, name)
}
