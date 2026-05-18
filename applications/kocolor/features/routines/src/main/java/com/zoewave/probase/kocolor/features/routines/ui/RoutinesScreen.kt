package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
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
            CenterAlignedTopAppBar(
                title = { Text("AM/PM Rituals", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    RoutineSection(
                        routine = uiState.morningRoutine,
                        allProducts = uiState.allProducts,
                        onEvent = onEvent
                    )
                }
                item {
                    RoutineSection(
                        routine = uiState.eveningRoutine,
                        allProducts = uiState.allProducts,
                        onEvent = onEvent
                    )
                }
            }
        }

        if (uiState.showEditDialog && uiState.activeEditRoutine != null) {
            EditRoutineDialog(uiState, onEvent)
        }
    }
}

@Composable
private fun RoutineSection(
    routine: BeautyRoutine?,
    allProducts: List<CosmeticItem>,
    onEvent: (RoutinesEvent) -> Unit
) {
    if (routine == null) return
    
    val timeIcon = if (routine.time == RoutineTime.MORNING) Icons.Default.LightMode else Icons.Default.NightsStay
    val accentColor = if (routine.time == RoutineTime.MORNING) Color(0xFFFFB74D) else Color(0xFF7986CB)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(timeIcon, null, modifier = Modifier.size(20.dp), tint = accentColor)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = routine.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    Text(text = routine.time.biologicalObjective, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                }
            }
            IconButton(onClick = { onEvent(RoutinesEvent.StartEditing(routine.id)) }) {
                Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            routine.steps.forEach { step ->
                StepItem(step, allProducts) {
                    onEvent(RoutinesEvent.ToggleStep(routine.id, step.id))
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    step: RoutineStep,
    allProducts: List<CosmeticItem>,
    onClick: () -> Unit
) {
    val linkedProducts = allProducts.filter { step.productIds.contains(it.id) }
    
    // Status Logic
    val status = when {
        linkedProducts.isEmpty() -> StepStatus.MISSING
        linkedProducts.any { it.isFinished } -> StepStatus.RED
        linkedProducts.any { it.usageCount > 50 } -> StepStatus.ORANGE // Placeholder logic
        else -> StepStatus.OK
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(status.color)
            )
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (step.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface
                )
                if (linkedProducts.isNotEmpty()) {
                    Text(
                        text = linkedProducts.joinToString { it.brand },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }

            if (step.isCompleted) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
            }
        }
    }
}

private enum class StepStatus(val color: Color) {
    OK(Color(0xFF4CAF50)),
    ORANGE(Color(0xFFFF9800)),
    RED(Color(0xFFF44336)),
    MISSING(Color.LightGray)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRoutineDialog(
    uiState: RoutinesUiState,
    onEvent: (RoutinesEvent) -> Unit
) {
    val routine = uiState.activeEditRoutine ?: return
    
    Dialog(
        onDismissRequest = { onEvent(RoutinesEvent.CloseEditDialog) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Edit ${routine.title}") },
                    navigationIcon = {
                        IconButton(onClick = { onEvent(RoutinesEvent.CloseEditDialog) }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Current Steps
                    items(routine.steps) { step ->
                        Column(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)).padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = step.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onEvent(RoutinesEvent.RemoveStep(routine.id, step.id)) }) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            
                            Text("LINKED PRODUCTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.allProducts) { product ->
                                    val isLinked = step.productIds.contains(product.id)
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { onEvent(RoutinesEvent.LinkProduct(routine.id, step.id, product.id)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (product.imageUrl != null) {
                                            AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize().background(product.colorHex?.let { parseColor(it) } ?: Color.Gray))
                                        }
                                        if (isLinked) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add New Step
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("ADD NEW STEP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                            OutlinedTextField(
                                value = uiState.draftStep.title,
                                onValueChange = { onEvent(RoutinesEvent.UpdateDraftStep(uiState.draftStep.copy(title = it))) },
                                label = { Text("Step Title (e.g. Double Cleanse)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            Button(
                                onClick = { onEvent(RoutinesEvent.AddStep(routine.id)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add Step")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutinesScreenPreview() {
    RoutinesScreen(
        uiState = RoutinesUiState(
            morningRoutine = BeautyRoutine(id = 1, title = "Morning Glow", time = RoutineTime.MORNING, steps = listOf(RoutineStep(title = "Cleanse")), date = 0L),
            eveningRoutine = BeautyRoutine(id = 2, title = "Night Repair", time = RoutineTime.EVENING, steps = listOf(RoutineStep(title = "Retinol")), date = 0L),
            isLoading = false
        ),
        onEvent = {},
        navTo = {}
    )
}
