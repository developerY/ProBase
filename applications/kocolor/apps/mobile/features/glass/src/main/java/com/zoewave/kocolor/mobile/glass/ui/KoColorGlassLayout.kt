package com.zoewave.kocolor.mobile.glass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.surface
import com.zoewave.probase.kocolor.model.RoutineStep
import kotlinx.coroutines.delay

@Composable
fun KoColorGlassLayout(
    uiState: GlassUiState,
    onEvent: (GlassUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstItemFocusRequester = remember { FocusRequester() }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val steps = uiState.morningRoutine.steps
                    items(steps.size) { index ->
                        val step = steps[index]
                        GlassRoutineStepItem(
                            step = step,
                            modifier = if (index == 0) Modifier.focusRequester(firstItemFocusRequester) else Modifier,
                            onClick = { onEvent(GlassUiEvent.ToggleStep(step.id)) }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            delay(500)
            try {
                firstItemFocusRequester.requestFocus()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

@Composable
fun GlassRoutineStepItem(
    step: RoutineStep,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .background(if (isFocused) GlimmerTheme.colors.primary.copy(alpha = 0.2f) else Color.Transparent)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (step.isCompleted) GlimmerTheme.colors.primary else GlimmerTheme.colors.outline
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = step.title,
                    style = GlimmerTheme.typography.bodyLarge,
                    fontWeight = if (isFocused) FontWeight.ExtraBold else if (step.isCompleted) FontWeight.Normal else FontWeight.Bold,
                    color = if (isFocused) Color.White else if (step.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White
                )
                if (step.description.isNotBlank()) {
                    Text(
                        text = step.description,
                        style = GlimmerTheme.typography.bodySmall,
                        color = if (isFocused) Color.White.copy(alpha = 0.8f) else GlimmerTheme.colors.outline
                    )
                }
            }
        }
    }
}
