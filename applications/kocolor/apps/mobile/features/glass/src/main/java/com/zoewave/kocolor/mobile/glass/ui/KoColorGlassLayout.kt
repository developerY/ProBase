package com.zoewave.kocolor.mobile.glass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.xr.glimmer.surface

@Composable
fun KoColorGlassLayout(
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
                    Text(
                        text = if (areVisualsOn) uiState.morningRoutine?.title ?: "Morning Ritual" else "Display Off",
                        style = GlimmerTheme.typography.titleMedium,
                        color = GlimmerTheme.colors.primary
                    )
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
                } else if (uiState.morningRoutine == null) {
                    Text(
                        text = "No routine scheduled for today.", 
                        style = GlimmerTheme.typography.bodyMedium,
                        color = Color.White
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.morningRoutine.steps.forEach { step ->
                            Button(
                                onClick = { onEvent(GlassUiEvent.ToggleStep(step.id)) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = step.title,
                                    style = GlimmerTheme.typography.bodyLarge,
                                    color = if (step.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White
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
private fun KoColorGlassLayoutPreview() {
    GlimmerTheme {
        KoColorGlassLayout(
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
                isLoading = false
            ),
            areVisualsOn = true,
            isVisualUiSupported = true,
            isPermissionDenied = false,
            onRetryPermission = {},
            onEvent = {}
        )
    }
}
