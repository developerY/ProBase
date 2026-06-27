package com.zoewave.probase.features.health.meals.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zoewave.probase.features.health.meals.ui.BioOptimizedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
private fun AddMealCameraScreenPreview() {
    MaterialTheme {
        AddMealCameraScreen(
            onCapture = {},
            onBack = {}
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddMealCameraScreen(
    onCapture: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val previewUseCase = remember { CameraPreview.Builder().build() }
    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

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
        LaunchedEffect(lifecycleOwner) {
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
                Log.e("AddMealCameraScreen", "Binding failed", e)
            }
        }

        Column(modifier = Modifier.fillMaxSize().background(BioOptimizedColors.Slate950)) {
            Box(modifier = Modifier.weight(1f)) {
                surfaceRequest?.let { request ->
                    CameraXViewfinder(
                        surfaceRequest = request,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Text(
                    text = "CAPTURE METABOLIC PROTOCOL",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                        .background(Color(0x80000000), MaterialTheme.shapes.medium)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = BioOptimizedColors.Cyan400,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 14.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val photoFile = createFile(context)
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imageCaptureUseCase.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        onCapture(Uri.fromFile(photoFile).toString())
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("AddMealCameraScreen", "Error: ${exception.message}")
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BioOptimizedColors.Cyan400),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("CAPTURE PHOTO", color = BioOptimizedColors.Slate950, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BioOptimizedColors.Slate700),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("CANCEL", color = Color.White)
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(BioOptimizedColors.Slate950), contentAlignment = Alignment.Center) {
            Text("Camera permission required.", color = Color.White)
        }
    }
}

private fun createFile(context: Context): File {
    val internalDir = context.filesDir
    val outputDir = File(internalDir, "CapturedMeals").apply { mkdirs() }
    val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    return File(outputDir, fileName)
}
