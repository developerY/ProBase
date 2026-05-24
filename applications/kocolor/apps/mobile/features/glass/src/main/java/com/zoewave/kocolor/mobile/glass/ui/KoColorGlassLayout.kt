package com.zoewave.kocolor.mobile.glass.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.surface
import com.zoewave.probase.kocolor.model.RoutineStep

@Composable
fun KoColorGlassLayout(
    uiState: GlassUiState,
    onEvent: (GlassUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black)
            .surface()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            title = {
                Text(
                    text = uiState.morningRoutine?.title ?: "Morning Ritual",
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
            if (uiState.isLoading) {
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
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.morningRoutine.steps.forEach { step ->
                        GlassRoutineStepItem(
                            step = step,
                            onClick = { onEvent(GlassUiEvent.ToggleStep(step.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassRoutineStepItem(
    step: RoutineStep,
    onClick: () -> Unit
) {
    ListItem(
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (step.isCompleted) GlimmerTheme.colors.primary else GlimmerTheme.colors.outline
            )
        },
        supportingLabel = {
            if (step.description.isNotBlank()) {
                Text(
                    text = step.description,
                    style = GlimmerTheme.typography.bodySmall,
                    color = GlimmerTheme.colors.outline
                )
            }
        },
        content = {
            Text(
                text = step.title,
                style = GlimmerTheme.typography.bodyLarge,
                fontWeight = if (step.isCompleted) FontWeight.Normal else FontWeight.Bold,
                color = if (step.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White
            )
        }
    )
}
