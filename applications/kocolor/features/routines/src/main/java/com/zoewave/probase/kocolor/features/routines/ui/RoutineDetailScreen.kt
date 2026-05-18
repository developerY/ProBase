package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: Long,
    uiState: RoutinesUiState,
    onEvent: (RoutinesEvent) -> Unit,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val routine = if (uiState.morningRoutine?.id == routineId) uiState.morningRoutine else uiState.eveningRoutine
    if (routine == null) return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(routine.title.uppercase(), style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { onEdit(routine.id) }) {
                        Icon(Icons.Default.Tune, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = routine.time.biologicalObjective,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = routine.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(routine.steps) { step ->
                StepItem(step, uiState.allProducts) {
                    onEvent(RoutinesEvent.ToggleStep(routine.id, step.id))
                }
            }
        }
    }
}
