package com.zoewave.probase.features.xr.xrglasses.ui.samples

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.CircleShape
import com.google.accompanist.permissions.rememberPermissionState

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
                .background(Color.Black),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        style = GlimmerTheme.typography.bodyMedium
                    )
                }

                if (uiState.transcribedText.isNotEmpty()) {
                    Text(
                        text = "Original: ${uiState.transcribedText}",
                        style = GlimmerTheme.typography.bodySmall,
                        color = GlimmerTheme.colors.secondary
                    )
                }

                val displayText = when {
                    uiState.isTranslating -> "Translating..."
                    uiState.translatedText.isNotEmpty() -> uiState.translatedText
                    uiState.isListening -> "Listening..."
                    else -> "Tap the mic to start"
                }

                Text(
                    text = displayText,
                    style = GlimmerTheme.typography.titleLarge,
                    color = GlimmerTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Control UI (Visible on phone/emulator)
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
                            contentDescription = "Microphone",
                            tint = if (uiState.isListening) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
