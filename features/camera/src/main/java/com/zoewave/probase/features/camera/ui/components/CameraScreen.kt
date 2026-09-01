package com.zoewave.probase.features.camera.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.Rational
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.zoewave.probase.features.camera.R
import com.zoewave.probase.features.camera.ui.CamEvent
import com.zoewave.probase.features.camera.ui.CamUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import com.zoewave.probase.core.ui.R as CoreUiR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    uiState: CamUIState,
    onEvent: (CamEvent) -> Unit,
    navTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Permission State
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // 2. Camera Use Cases
    val previewUseCase = remember { Preview.Builder().build() }
    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var savedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 3. Lifecycle-Aware Orientation Logic (Prevents battery drain)
    val orientationEventListener = remember {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCaptureUseCase.targetRotation = rotation
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) orientationEventListener.enable()
            else if (event == Lifecycle.Event.ON_STOP) orientationEventListener.disable()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            orientationEventListener.disable()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        LaunchedEffect(lifecycleOwner, uiState.cameraSelector) {
            val cameraProvider = ProcessCameraProvider.awaitInstance(context)
            previewUseCase.surfaceProvider = null
            previewUseCase.setSurfaceProvider(ContextCompat.getMainExecutor(context)) { request ->
                surfaceRequest = request
            }

            try {
                cameraProvider.unbindAll()

                val viewPort = ViewPort.Builder(
                    Rational(9, 16),
                    Surface.ROTATION_0
                ).setScaleType(ViewPort.FILL_CENTER).build()

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(previewUseCase)
                    .addUseCase(imageCaptureUseCase)
                    .setViewPort(viewPort)
                    .build()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    uiState.cameraSelector,
                    useCaseGroup
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "Binding failed", e)
            }
        }

        Column(modifier = modifier.fillMaxSize()) {
            // Camera Preview Viewfinder
            Box(modifier = Modifier.weight(1f)) {
                surfaceRequest?.let { request ->
                    CameraXViewfinder(
                        surfaceRequest = request,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Conditional Face Reticle Overlay
                if (uiState.target == "face" || uiState.target == "face_simulator") {
                    FaceReticleOverlay()
                }
            }

            // Capture Button & Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        // ✅ FIX: Move File IO off the Main Thread
                        coroutineScope.launch(Dispatchers.IO) {
                            val photoFile = createFile(context)
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imageCaptureUseCase.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        savedImageUri = Uri.fromFile(photoFile)
                                        onEvent(CamEvent.AddItem(
                                            name = context.getString(R.string.features_camera_photo_label),
                                            description = context.getString(R.string.features_camera_photo_desc),
                                            imgPath = savedImageUri.toString()
                                        ))
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraScreen", "Error: ${exception.message}", exception)
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.features_camera_capture_photo))
                }

                // ✅ FIX: Use Coil to safely load the high-res image
                savedImageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = uri,
                        contentDescription = stringResource(R.string.features_camera_cd_captured_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    } else {
        // Permission Denied State
        val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
            stringResource(R.string.features_camera_permission_rationale)
        } else {
            stringResource(R.string.features_camera_permission_denied)
        }

        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
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
                Text(
                    if (cameraPermissionState.status.shouldShowRationale) 
                        stringResource(CoreUiR.string.action_grant_permissions) 
                    else 
                        stringResource(CoreUiR.string.action_open_settings)
                )
            }
        }
    }
}

@Composable
private fun FaceReticleOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 2.dp.toPx()
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
        
        // Dark translucent overlay
        drawRect(color = Color.Black.copy(alpha = 0.4f))
        
        // Face reticle (clear area)
        val reticleWidth = size.width * 0.7f
        val reticleHeight = size.height * 0.5f
        val left = (size.width - reticleWidth) / 2
        val top = (size.height - reticleHeight) / 3
        
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(reticleWidth, reticleHeight),
            cornerRadius = CornerRadius(24.dp.toPx()),
            blendMode = BlendMode.Clear
        )
        
        // Dashed border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(reticleWidth, reticleHeight),
            cornerRadius = CornerRadius(24.dp.toPx()),
            style = Stroke(width = strokeWidth, pathEffect = dashEffect)
        )
    }
}

private fun createFile(context: Context): File {
    // 1. Force the use of the strictly internal, sandboxed directory
    val internalDir = context.filesDir
    // 2. Create your dedicated app folder inside that sandbox
    val outputDir = File(internalDir, "CapturedImages").apply { mkdirs() }
    // 3. Generate the file name
    val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(outputDir, fileName)
}

private fun createFileInternalOptional(context: Context): File {
    val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
    val outputDir = File(mediaDir, "BaseProImages").apply { mkdirs() }
    val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(outputDir, fileName)
}