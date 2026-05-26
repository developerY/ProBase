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
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.VoiceInputIndicator
import androidx.xr.glimmer.stack.VerticalStack
import androidx.xr.glimmer.stack.items
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Title) - High contrast for AI glasses
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (areVisualsOn) {
                            uiState.morningRoutine?.title?.replaceFirstChar { it.uppercase() } ?: "Morning Ritual"
                        } else {
                            "Display Off"
                        },
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

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = GlimmerTheme.colors.primary
                        )
                    } else if (uiState.morningRoutine == null) {
                        Text(
                            text = "No ritual scheduled.",
                            style = GlimmerTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // Refined: VerticalStack for focused step-by-step ritual
                        VerticalStack(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
                        ) {
                            val steps = uiState.morningRoutine.steps
                            items(steps, key = { it.id }) { step ->
                                Card(
                                    onClick = { onEvent(GlassUiEvent.ToggleStep(step.id)) },
                                    modifier = Modifier.fillMaxWidth().itemDecoration(GlimmerTheme.shapes.medium),
                                    title = {
                                        Text(
                                            text = step.title,
                                            color = if (step.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (step.isCompleted) GlimmerTheme.colors.primary else Color.White
                                        )
                                    }
                                ) {
                                    if (step.description.isNotBlank()) {
                                        Text(
                                            text = step.description,
                                            style = GlimmerTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Global Controls at the bottom - Clear distance from the stack
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { onEvent(GlassUiEvent.ToggleAi) }) {
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
        } else {
            Text("Audio Guidance Mode Active", color = Color.White)
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
            onEvent = {}
        )
    }
}
