package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*

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
                title = { Text("AM/PM RITUALS", style = MaterialTheme.typography.labelLarge, letterSpacing = 4.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Home) }) { Icon(Icons.Default.Home, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            uiState.morningRoutine?.let { routine ->
                HeroRoutineCard(
                    routine = routine,
                    onClick = { navTo(KoColorRoute.RoutineDetail(routine.id)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            uiState.eveningRoutine?.let { routine ->
                HeroRoutineCard(
                    routine = routine,
                    onClick = { navTo(KoColorRoute.RoutineDetail(routine.id)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroRoutineCard(
    routine: BeautyRoutine,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMorning = routine.time == RoutineTime.MORNING
    val accentColor = if (isMorning) Color(0xFFFFB74D) else Color(0xFF7986CB)
    val bgBrush = Brush.verticalGradient(
        colors = if (isMorning) {
            listOf(Color(0xFFFFF9C4).copy(alpha = 0.3f), Color.White)
        } else {
            listOf(Color(0xFFE8EAF6).copy(alpha = 0.3f), Color.White)
        }
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(bgBrush).padding(32.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isMorning) Icons.Default.LightMode else Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = routine.time.biologicalObjective.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = routine.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp
                    )
                }
                
                Text(
                    text = routine.time.objectiveDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
            
            // Completion Status
            val completedSteps = routine.steps.count { it.isCompleted }
            val totalSteps = routine.steps.size
            if (totalSteps > 0) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    color = if (completedSteps == totalSteps) Color.Green.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$completedSteps / $totalSteps STEPS",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StepItem(
    step: RoutineStep,
    allProducts: List<CosmeticItem>,
    onClick: () -> Unit
) {
    val statusColor = when {
        !step.isCompleted && step.productIds.isEmpty() -> StepStatus.MISSING.color
        !step.isCompleted -> MaterialTheme.colorScheme.outlineVariant
        else -> StepStatus.OK.color
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (step.isCompleted) FontWeight.Normal else FontWeight.Bold,
                        modifier = Modifier.alpha(if (step.isCompleted) 0.5f else 1f)
                    )
                    if (step.productIds.isNotEmpty()) {
                        val linkedCount = step.productIds.size
                        Text(
                            text = "$linkedCount linked product${if (linkedCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            if (step.isCompleted) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(24.dp))
            }
        }
    }
}

enum class StepStatus(val color: Color) {
    OK(Color(0xFF4CAF50)),
    ORANGE(Color(0xFFFF9800)),
    RED(Color(0xFFF44336)),
    MISSING(Color(0xFFB0BEC5))
}

@Preview(showBackground = true)
@Composable
private fun RoutinesScreenPreview() {
    RoutinesScreen(
        uiState = RoutinesUiState(
            morningRoutine = BeautyRoutine(title = "Morning beautiful routine", time = RoutineTime.MORNING, steps = emptyList(), date = 0),
            eveningRoutine = BeautyRoutine(title = "Evening restoration", time = RoutineTime.EVENING, steps = emptyList(), date = 0)
        ),
        onEvent = {},
        navTo = {}
    )
}
