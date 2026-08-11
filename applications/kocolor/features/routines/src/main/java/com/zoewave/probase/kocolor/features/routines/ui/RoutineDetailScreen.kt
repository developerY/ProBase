package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.routines.R
import com.zoewave.probase.kocolor.features.routines.ui.components.*
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.core.model.ritual.RoutineStep
import com.zoewave.probase.core.model.ritual.RoutineTime

data class RoutineDetailUiState(
    val routineId: Long,
    val routinesUiState: RoutinesUiState
)

@Preview(showBackground = true)
@Composable
private fun RoutineDetailScreenPreview() {
    MaterialTheme {
        RoutineDetailScreen(
            uiState = RoutineDetailUiState(
                routineId = 1L,
                routinesUiState = RoutinesUiState(
                    morningRoutine = BeautyRoutine(id = 1L, title = "Morning", time = RoutineTime.MORNING, steps = emptyList(), date = 0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    uiState: RoutineDetailUiState,
    modifier: Modifier = Modifier,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val routineId = uiState.routineId
    val state = uiState.routinesUiState
    val routine = when (routineId) {
        state.morningRoutine?.id -> state.morningRoutine
        state.mealsRoutine?.id -> state.mealsRoutine
        state.eveningRoutine?.id -> state.eveningRoutine
        else -> null
    }
    if (routine == null) return

    val accentColor = when (routine.time) {
        RoutineTime.MORNING -> Color(0xFF6B705C)
        RoutineTime.MEALS -> Color(0xFFE0C097)
        RoutineTime.EVENING -> Color(0xFF457B9D)
        else -> Color.Gray
    }

    var selectedInfoStep by remember { mutableStateOf<RoutineStep?>(null) }

    if (selectedInfoStep != null) {
        AlertDialog(
            onDismissRequest = { selectedInfoStep = null },
            confirmButton = {
                TextButton(onClick = { selectedInfoStep = null }) {
                    Text(stringResource(R.string.applications_kocolor_features_routines_done), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column {
                    Text(
                        text = selectedInfoStep!!.subtitle ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedInfoStep!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = selectedInfoStep!!.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_routines_serene_rituals), style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    GlassConnectionHeaderAction(
                        buttonState = state.glassButtonState,
                        onButtonClick = { onEvent(RoutinesEvent.ProjectToGlass(routine.time)) },
                        modifier = Modifier.size(40.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (routine.time) {
                                        RoutineTime.MORNING -> Icons.Default.LightMode
                                        RoutineTime.MEALS -> Icons.Default.Restaurant
                                        else -> Icons.Default.NightsStay
                                    },
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = when (routine.time) {
                                        RoutineTime.MORNING -> stringResource(R.string.applications_kocolor_features_routines_current_ritual)
                                        RoutineTime.MEALS -> stringResource(R.string.applications_kocolor_features_routines_bio_sync_ritual)
                                        else -> stringResource(R.string.applications_kocolor_features_routines_evening_ritual_label)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = when (routine.time) {
                                    RoutineTime.MORNING -> stringResource(R.string.applications_kocolor_features_routines_morning_ritual)
                                    RoutineTime.MEALS -> stringResource(R.string.applications_kocolor_features_routines_meals_ritual)
                                    else -> stringResource(R.string.applications_kocolor_features_routines_evening_ritual)
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        val completedCount = routine.steps.count { it.isCompleted }
                        val totalCount = routine.steps.size
                        val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = accentColor.copy(alpha = 0.1f),
                                strokeWidth = 4.dp
                            )
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                color = accentColor,
                                strokeWidth = 4.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = stringResource(R.string.applications_kocolor_features_routines_progress_format, completedCount, totalCount), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_routines_done), 
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    modifier = Modifier.alpha(0.5f)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = when (routine.time) {
                            RoutineTime.MORNING -> stringResource(R.string.applications_kocolor_features_routines_morning_desc)
                            else -> stringResource(R.string.applications_kocolor_features_routines_evening_desc)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 22.sp
                    )
                }
            }

            itemsIndexed(routine.steps, key = { _, step -> step.id }) { index, step ->
                val linkedProduct = state.allProducts.find { step.productIds.contains(it.internalId) }
                
                SplitRitualStep(
                    uiState = SplitRitualStepUiState(
                        step = step,
                        linkedProduct = linkedProduct,
                        isReorderMode = false
                    ),
                    modifier = Modifier.shadow(0.dp),
                    onEvent = { onEvent(RoutinesEvent.ToggleStep(routine.id, step.id)) },
                    onInfoClick = { selectedInfoStep = it },
                    navTo = { navTo(KoColorRoute.RoutineEditor(routineId, step.id)) }
                )
            }
            
            item { DailyInsightSmall(uiState = Unit, onEvent = {}, navTo = {}) }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}
