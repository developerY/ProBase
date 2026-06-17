package com.zoewave.probase.features.glass.translation.ui

import android.Manifest
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.xr.glimmer.DepthEffect
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import android.app.Activity
import androidx.xr.projected.ProjectedDisplayController
import androidx.xr.projected.ProjectedDisplayController.PresentationMode
import java.util.function.Consumer
import androidx.compose.ui.platform.LocalContext

/**
 * A Glimmer-optimized translation screen for AI Glasses (Display Glasses).
 * 
 * Design Principles:
 * 1. Additive Display: Black background (#000000) is transparent.
 * 2. Field of View: Subtitles are pinned to the bottom.
 * 3. Legibility: Glimmer typography is used.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalProjectedApi::class)
@Composable
fun TranslationScreen(
    viewModel: TranslationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val context = LocalContext.current
    
    // DEBUG: Wear State Protocol (Using PresentationMode as proxy for WearState)
    var visualsOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            val activity = context as? Activity ?: return@LaunchedEffect
            val controller = ProjectedDisplayController.create(activity)
            controller.addPresentationModeChangedListener { flags ->
                visualsOn = flags.hasPresentationMode(PresentationMode.VISUALS_ON)
            }
        } catch (_: Exception) {
            // Fallback for non-projected context
        }
    }

    GlimmerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black), // Transparent on glasses
            contentAlignment = Alignment.BottomCenter
        ) {
            // DEBUG OVERLAY (Step 1 of Protocol)
            Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = "DEBUG: VisualsOn (Proxy for WearState) = $visualsOn", 
                    color = Color.Yellow, 
                    style = GlimmerTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error indicator
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        style = GlimmerTheme.typography.bodySmall
                    )
                }

                // Transcription feedback (smaller)
                if (uiState.transcribedText.isNotEmpty()) {
                    Text(
                        text = "Detected: ${uiState.transcribedText}",
                        style = GlimmerTheme.typography.bodyMedium,
                        color = GlimmerTheme.colors.secondary
                    )
                }

                // Main Translation result
                val displayMsg = when {
                    uiState.isTranslating -> "Translating..."
                    uiState.translatedText.isNotEmpty() -> uiState.translatedText
                    uiState.isListening -> "Listening..."
                    else -> "Ready to Translate"
                }

                Text(
                    text = displayMsg,
                    style = GlimmerTheme.typography.titleLarge,
                    color = GlimmerTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Glimmer-native IconButton for touchpad support
                IconButton(
                    onClick = {
                        if (micPermissionState.status.isGranted) {
                            if (uiState.isListening) viewModel.stopListening() else viewModel.startListening()
                        } else {
                            micPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Microphone",
                        tint = if (uiState.isListening) GlimmerTheme.colors.primary else GlimmerTheme.colors.secondary
                    )
                }
            }
        }
    }
}
