package com.zoewave.probase.features.glass.vision.ui

import android.app.Activity
import android.util.Log
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.ExperimentalLensFacing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.IconButton
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.TitleChip

/**
 * The entry point for the Vision screen.
 *
 * ARCHITECTURAL NOTE: This route runs on the Glasses.
 * It must NOT request permissions or initialize the CameraManager.
 * The host phone app handles the Context Bridge setup.
 */
@ExperimentalLensFacing
@ExperimentalCamera2Interop
@OptIn(androidx.xr.projected.experimental.ExperimentalProjectedApi::class)
@Composable
fun VisionRoute(
    viewModel: VisionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    VisionScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

/**
 * A Glimmer-optimized vision screen for AI Glasses (Stateless).
 */
@ExperimentalLensFacing
@ExperimentalCamera2Interop
@OptIn(androidx.xr.projected.experimental.ExperimentalProjectedApi::class)
@Composable
fun VisionScreen(
    uiState: VisionUiState,
    onEvent: (VisionUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // DEBUG: Wear State Protocol
    var visualsOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            val controller = androidx.xr.projected.ProjectedDisplayController.create(activity ?: return@LaunchedEffect)
            controller.addPresentationModeChangedListener { flags ->
                visualsOn = flags.hasPresentationMode(androidx.xr.projected.ProjectedDisplayController.PresentationMode.VISUALS_ON)
            }
        } catch (_: Exception) {}
    }

    GlimmerTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black), // Pure Black ensures total optical transparency
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
                        text = uiState.error,
                        color = Color.Red,
                        style = GlimmerTheme.typography.bodySmall
                    )
                }

                // Result description
                val displayMsg = when {
                    uiState.isAnalyzing -> "Analyzing..."
                    uiState.isCapturing -> "Capturing..."
                    uiState.imageDescription.isNotEmpty() -> uiState.imageDescription
                    else -> "Ready"
                }

                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = { onEvent(VisionUiEvent.TriggerCapture) }
                ) {
                    Text(
                        text = displayMsg,
                        style = GlimmerTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Capture Button
                IconButton(
                    onClick = {
                        Log.d("VisionUI", "GLASSES_ICON_CLICKED (Glimmer UI)")
                        // We assume the phone already handled permissions. Just fire the capture!
                        onEvent(VisionUiEvent.TriggerCapture)
                    }
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
