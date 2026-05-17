package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.zoewave.probase.kocolor.features.routines.data.RoutineDefaults
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime

@Composable
fun RoutinesUiRoute(
    uiState: Unit = Unit,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit
) {
    val viewModel: RoutinesViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RoutinesScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    uiState: RoutinesUiState,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beauty Routines") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                RoutinesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RoutinesUiState.Success -> {
                    RoutinesContent(
                        uiState = uiState.morningRoutine to uiState.eveningRoutine,
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
            }
        }
    }
}

@Composable
fun RoutinesContent(
    uiState: Pair<BeautyRoutine?, BeautyRoutine?>,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val morningRoutine = uiState.first
    val eveningRoutine = uiState.second
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BeautyAdviceCard(
                uiState = Unit,
                onEvent = {},
                navTo = {}
            )
        }

        if (morningRoutine != null) {
            item {
                RoutineCard(
                    uiState = morningRoutine,
                    onEvent = { stepId -> onEvent(RoutinesEvent.ToggleStep(morningRoutine, stepId)) },
                    navTo = navTo
                )
            }
        }

        if (eveningRoutine != null) {
            item {
                RoutineCard(
                    uiState = eveningRoutine,
                    onEvent = { stepId -> onEvent(RoutinesEvent.ToggleStep(eveningRoutine, stepId)) },
                    navTo = navTo
                )
            }
        }
    }
}

@Composable
fun BeautyAdviceCard(
    uiState: Unit,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val advice = remember { RoutineDefaults.morningAdvice.random() } // Quick fix for build, will match time in Home

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Beauty Tip of the Day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = advice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun RoutineCard(
    uiState: BeautyRoutine,
    onEvent: (String) -> Unit,
    navTo: (KoColorRoute) -> Unit
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
                    text = uiState.title,
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
                    uiState.steps.forEach { step ->
                        StepItem(
                            uiState = step,
                            onEvent = { onEvent(step.id) },
                            navTo = navTo
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(
    uiState: RoutineStep,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Surface(
        onClick = { onEvent(Unit) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (uiState.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (uiState.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (uiState.isRecommended) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (uiState.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                if (uiState.description.isNotEmpty()) {
                    Text(
                        text = uiState.description,
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
private fun BeautyAdviceCardPreview() {
    MaterialTheme {
        BeautyAdviceCard(uiState = Unit, onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun StepItemPreview() {
    MaterialTheme {
        StepItem(
            uiState = RoutineStep("1", "Step Title", "Description"),
            onEvent = {},
            navTo = {}
        )
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
            navTo = {}
        )
    }
}
