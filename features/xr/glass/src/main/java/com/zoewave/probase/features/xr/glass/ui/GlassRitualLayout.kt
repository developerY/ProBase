package com.zoewave.probase.features.xr.glass.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.VoiceInputIndicator
import androidx.xr.glimmer.list.GlimmerLazyColumn
import androidx.xr.glimmer.surface

@Composable
fun GlassRitualLayout(
    uiState: GlassUiState,
    areVisualsOn: Boolean,
    isVisualUiSupported: Boolean,
    isPermissionDenied: Boolean,
    onRetryPermission: () -> Unit,
    onEvent: (GlassUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .surface()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isPermissionDenied) {
            Card(
                title = { Text("Permissions") },
                action = { Button(onClick = { onEvent(GlassUiEvent.CloseApp) }) { Text("Exit") } }
            ) {
                Text("Camera access is needed to use AI glasses features.")
                Button(onClick = onRetryPermission) { Text("Retry") }
            }
        } else if (isVisualUiSupported) {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (areVisualsOn) "Morning Ritual" else "Display Off",
                            style = GlimmerTheme.typography.titleMedium,
                            color = GlimmerTheme.colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.isAiActive) {
                            VoiceInputIndicator(
                                level = { uiState.aiAudioLevel },
                                indicatorColor = GlimmerTheme.colors.primary
                            )
                        }
                    }
                },
                action = {
                    Button(onClick = { onEvent(GlassUiEvent.CloseApp) }) {
                        Text("Exit", style = GlimmerTheme.typography.bodyMedium)
                    }
                }
            ) {
                if (!areVisualsOn) {
                    Text("Audio Guidance Mode Active. Please follow the spoken instructions.")
                } else if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GlimmerTheme.colors.primary)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Swipe to scroll, click to toggle:")
                        
                        GlimmerLazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentPadding = PaddingValues(top = 16.dp)
                        ) {
                            if (uiState.morningRoutine != null) {
                                uiState.morningRoutine.steps.forEach { step ->
                                    item {
                                        ListItem(
                                            onClick = { 
                                                android.util.Log.d("KoColorGlass", "ListItem clicked: ${step.title}")
                                                onEvent(GlassUiEvent.ToggleStep(step.id)) 
                                            },
                                            content = {
                                                Text(
                                                    text = "${if (step.isCompleted) "✓ " else "○ "}${step.title}",
                                                    style = GlimmerTheme.typography.bodyLarge,
                                                    color = Color.White
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Text("No routine data found.", color = Color.White)
                                }
                            }
                        }
                        
                        Button(
                            onClick = { onEvent(GlassUiEvent.ToggleAi) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (uiState.isAiActive) "Stop Gemini" else "Talk to Gemini")
                            }
                        }
                    }
                }
            }
        } else {
            Text("Audio Guidance Mode Active (No Visual Support)", color = Color.White)
        }
    }
}

@Preview(showBackground = true, widthDp = 480, heightDp = 480)
@Composable
private fun GlassRitualLayoutPreview() {
    GlimmerTheme {
        GlassRitualLayout(
            uiState = GlassUiState(
                morningRoutine = com.zoewave.probase.kocolor.model.BeautyRoutine(
                    title = "morning beautiful routine",
                    time = com.zoewave.probase.kocolor.model.RoutineTime.MORNING,
                    steps = listOf(
                        com.zoewave.probase.kocolor.model.RoutineStep(
                            title = "Wake Up",
                            description = "No snooze, open curtains for natural light.",
                            isCompleted = true
                        ),
                        com.zoewave.probase.kocolor.model.RoutineStep(
                            title = "Hydrate",
                            description = "Drink water (optionally with lemon).",
                            isCompleted = false
                        ),
                        com.zoewave.probase.kocolor.model.RoutineStep(
                            title = "Move",
                            description = "10 min yoga or light stretch.",
                            isCompleted = false
                        )
                    ),
                    date = 0
                ),
                isLoading = false,
                isAiActive = false,
                aiAudioLevel = 0f
            ),
            areVisualsOn = true,
            isVisualUiSupported = true,
            isPermissionDenied = false,
            onRetryPermission = {},
            onEvent = {}
        )
    }
}
