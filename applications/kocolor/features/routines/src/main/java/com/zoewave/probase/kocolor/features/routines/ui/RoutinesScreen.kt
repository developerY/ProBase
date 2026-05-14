package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime

@Composable
fun RoutinesUiRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RoutinesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = { /* Handle navigation if any */ },
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    uiState: RoutinesUiState,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Beauty Routines") })
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                RoutinesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RoutinesUiState.Success -> {
                    RoutinesContent(
                        morningRoutine = uiState.morningRoutine,
                        eveningRoutine = uiState.eveningRoutine,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutinesContent(
    morningRoutine: BeautyRoutine?,
    eveningRoutine: BeautyRoutine?,
    onEvent: (RoutinesEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BeautyAdviceCard()
        }

        if (morningRoutine != null) {
            item {
                RoutineCard(
                    routine = morningRoutine,
                    onToggleStep = { stepId -> onEvent(RoutinesEvent.ToggleStep(morningRoutine, stepId)) }
                )
            }
        }

        if (eveningRoutine != null) {
            item {
                RoutineCard(
                    routine = eveningRoutine,
                    onToggleStep = { stepId -> onEvent(RoutinesEvent.ToggleStep(eveningRoutine, stepId)) }
                )
            }
        }
    }
}

@Composable
private fun BeautyAdviceCard() {
    val tips = listOf(
        "Consistency is key: Perform your routine daily for best results.",
        "Hydration from within: Drink at least 8 glasses of water a day.",
        "Pat, don't rub: Gently pat skin products to aid absorption.",
        "The 10-Step Rule: Korean skincare emphasizes layering for deep nourishment.",
        "Sunscreen always: Even on cloudy days, UV rays can damage skin."
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Beauty Advice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            tips.forEach { tip ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•", modifier = Modifier.padding(end = 8.dp))
                    Text(tip, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: BeautyRoutine,
    onToggleStep: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    routine.steps.forEach { step ->
                        StepItem(
                            step = step,
                            onToggle = { onToggleStep(step.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    step: RoutineStep,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (step.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (step.isRecommended) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (step.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                if (step.description.isNotEmpty()) {
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutinesScreenPreview() {
    MaterialTheme {
        RoutinesScreen(
            uiState = RoutinesUiState.Success(
                morningRoutine = BeautyRoutine(
                    title = "Morning Routine",
                    time = RoutineTime.MORNING,
                    steps = listOf(
                        RoutineStep("1", "Brush Teeth", "Common hygiene."),
                        RoutineStep("2", "Sunscreen", "Korean Skincare", isRecommended = true)
                    ),
                    date = 0
                ),
                eveningRoutine = BeautyRoutine(
                    title = "Evening Routine",
                    time = RoutineTime.EVENING,
                    steps = listOf(
                        RoutineStep("1", "Double Cleanse", "Korean Skincare", isRecommended = true)
                    ),
                    date = 0
                )
            ),
            onEvent = {},
            navTo = {},
            onBack = {}
        )
    }
}
