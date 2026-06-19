package com.zoewave.probase.features.glass.vision.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.IconButton
import androidx.xr.glimmer.TitleChip
import androidx.xr.glimmer.TitleChip
import androidx.xr.projected.ProjectedDisplayController
import androidx.xr.projected.ProjectedDisplayController.PresentationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.Manifest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * A Glimmer-optimized vision screen for AI Glasses.
 */
@OptIn(ExperimentalPermissionsApi::class, androidx.xr.projected.experimental.ExperimentalProjectedApi::class)
@Composable
fun VisionScreen(
    viewModel: VisionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val activity = context as? Activity

    // DEBUG: Wear State Protocol
    var visualsOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            val controller = ProjectedDisplayController.create(activity ?: return@LaunchedEffect)
            controller.addPresentationModeChangedListener { flags ->
                visualsOn = flags.hasPresentationMode(PresentationMode.VISUALS_ON)
            }
        } catch (_: Exception) {}
    }

    // Sync permission status with ViewModel
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        viewModel.updatePermissionStatus(cameraPermissionState.status.isGranted)
        if (cameraPermissionState.status.isGranted && activity != null) {
            viewModel.setupCamera(activity)
        }
    }

    GlimmerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black), // Transparent on glasses
            contentAlignment = Alignment.BottomCenter
        ) {
            // DEBUG OVERLAY
            Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = "Vision Active: VisualsOn=$visualsOn", 
                    color = Color.Yellow, 
                    style = GlimmerTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp) // Optimized Glimmer spacing
            ) {
                TitleChip {
                    Text("Vision AI")
                }

                // Error indicator
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        style = GlimmerTheme.typography.bodySmall
                    )
                }

                // Result description
                val displayMsg = when {
                    uiState.isAnalyzing -> "Analyzing image..."
                    uiState.isCapturing -> "Capturing..."
                    uiState.imageDescription.isNotEmpty() -> uiState.imageDescription
                    else -> "Tap camera to describe what you see"
                }

                Text(
                    text = displayMsg,
                    style = GlimmerTheme.typography.titleMedium, // Better fit for HUD
                    color = GlimmerTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Capture Button
                IconButton(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            viewModel.takePicture()
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Picture",
                        tint = if (uiState.isCapturing || uiState.isAnalyzing) 
                            GlimmerTheme.colors.secondary else GlimmerTheme.colors.primary
                    )
                }
            }
        }
    }
}
