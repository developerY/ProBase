package com.zoewave.probase.features.xr.glass.translation.ui

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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.CircleShape

/**
 * A Glimmer-optimized translation screen for AI Glasses (Display Glasses).
 * 
 * Design Principles:
 * 1. Additive Display: Black background (#000000) is transparent.
 * 2. Field of View: Subtitles are pinned to the bottom.
 * 3. Legibility: Glimmer typography is used.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TranslationScreen(
    viewModel: TranslationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    GlimmerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black), // Transparent on glasses
            contentAlignment = Alignment.BottomCenter
        ) {
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

                // Control UI (Rendered on host/phone side or as interactive overlay)
                Surface(
                    color = if (uiState.isListening) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                ) {
                    IconButton(
                        onClick = {
                            if (micPermissionState.status.isGranted) {
                                if (uiState.isListening) viewModel.stopListening() else viewModel.startListening()
                            } else {
                                micPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Toggle Microphone",
                            tint = if (uiState.isListening) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
