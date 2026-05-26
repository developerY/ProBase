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
    onEvent: (GlassUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .surface()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isVisualUiSupported) {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (areVisualsOn) "Morning Ritual" else "Display Off",
                            style = GlimmerTheme.typography.titleMedium,
                            color = GlimmerTheme.colors.primary
                        )
                        if (uiState.isAiActive) {
                            VoiceInputIndicator(
                                level = { uiState.aiAudioLevel },
                                indicatorColor = GlimmerTheme.colors.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                },
                action = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onEvent(GlassUiEvent.ToggleAi) },
                            modifier = Modifier.width(140.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (uiState.isAiActive) GlimmerTheme.colors.primary else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (uiState.isAiActive) "Stop Gemini" else "Talk to Gemini")
                            }
                        }
                        Button(onClick = { onEvent(GlassUiEvent.CloseApp) }) {
                            Text("Exit")
                        }
                    }
                }
            ) {
                if (!areVisualsOn) {
                    Text("Audio Guidance Mode Active. Gemini is listening for your command.")
                } else if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GlimmerTheme.colors.primary)
                    }
                } else if (uiState.morningRoutine == null) {
                    Text(
                        text = "No ritual scheduled. Start one on your phone.", 
                        style = GlimmerTheme.typography.bodyMedium,
                        color = Color.White
                    )
                } else {
                    GlimmerLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp)
                    ) {
                        uiState.morningRoutine.steps.forEach { step ->
                            item {
                                ListItem(
                                    onClick = { onEvent(GlassUiEvent.ToggleStep(step.id)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (step.isCompleted) GlimmerTheme.colors.primary else Color.White
                                        )
                                    },
                                    content = {
                                        Text(
                                            text = step.title,
                                            style = GlimmerTheme.typography.bodyLarge,
                                            color = if (step.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White
                                        )
                                    },
                                    supportingLabel = {
                                        if (step.description.isNotBlank()) {
                                            Text(
                                                text = step.description,
                                                style = GlimmerTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                )
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
                isAiActive = true,
                aiAudioLevel = 0.5f
            ),
            areVisualsOn = true,
            isVisualUiSupported = true,
            onEvent = {}
        )
    }
}
