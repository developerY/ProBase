package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Reorder
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.routines.ui.components.DailyInsightSmall
import com.zoewave.probase.kocolor.features.routines.ui.components.GlassConnectionHeaderAction
import com.zoewave.probase.kocolor.features.routines.ui.components.SplitRitualStep
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.RoutineTime
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val routineId = uiState.routineId
    val state = uiState.routinesUiState
    val routine = if (state.morningRoutine?.id == routineId) state.morningRoutine else state.eveningRoutine
    if (routine == null) return

    val isMorning = routine.time == RoutineTime.MORNING
    val accentColor = if (isMorning) Color(0xFF6B705C) else Color(0xFF457B9D)

    var isReorderMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Serene Rituals", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    GlassConnectionHeaderAction(
                        buttonState = state.glassButtonState,
                        onButtonClick = { onEvent(RoutinesEvent.ProjectToGlass(routine.time)) },
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { isReorderMode = !isReorderMode }) {
                        Icon(if (isReorderMode) Icons.Default.Check else Icons.Default.Reorder, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (!isReorderMode) {
                LargeFloatingActionButton(
                    onClick = { navTo(KoColorRoute.RoutineEditor(routineId, "new_step")) },
                    shape = CircleShape,
                    containerColor = accentColor,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Ritual Stage",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { padding ->
        val lazyListState = rememberLazyListState()
        val reorderableState = rememberReorderableLazyListState(
            lazyListState = lazyListState,
            onMove = { from, to ->
                onEvent(RoutinesEvent.ReorderSteps(routineId, from.index - 1, to.index - 1))
            }
        )

        LazyColumn(
            state = lazyListState,
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
                                    imageVector = if (isMorning) Icons.Default.LightMode else Icons.Default.NightsStay,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "CURRENT RITUAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = if (isMorning) "Morning Ritual" else "Evening Ritual",
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
                                Text(text = "$completedCount/$totalCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                Text(
                                    text = "DONE", 
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    modifier = Modifier.alpha(0.5f)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Every step is an act of care. Complete sequence to prepare for a balanced day ahead.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 22.sp
                    )
                }
            }

            itemsIndexed(routine.steps, key = { _, step -> step.id }) { index, step ->
                ReorderableItem(reorderableState, key = step.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    
                    val linkedProduct = state.allProducts.find { step.productIds.contains(it.id) }
                    
                    SplitRitualStep(
                        uiState = Triple(step, linkedProduct, isReorderMode),
                        onEvent = { onEvent(RoutinesEvent.ToggleStep(routine.id, step.id)) },
                        navTo = { navTo(KoColorRoute.RoutineEditor(routineId, step.id)) },
                        modifier = Modifier
                            .shadow(elevation)
                            .then(if (isReorderMode) Modifier.draggableHandle() else Modifier)
                    )
                }
            }
            
            item { DailyInsightSmall(uiState = Unit, onEvent = {}, navTo = {}) }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}
