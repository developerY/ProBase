package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    val isMorning = routine.time == RoutineTime.MORNING
    val accentColor = if (isMorning) Color(0xFF6B705C) else Color(0xFF457B9D)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Serene Rituals", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { onEdit(routine.id) }) { Icon(Icons.Default.Tune, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
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
                                text = if (isMorning) "Your Morning Ritual" else "Your Evening Ritual",
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Progress Indicator
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
                        text = "Every step is an act of care. Complete your sequence to prepare for a balanced day ahead.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 22.sp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (isMorning) "AM Essentials" else "Evening Ritual",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "5 mins remaining",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.5f)
                    )
                }
            }

            items(routine.steps) { step ->
                CleanRitualStep(step) {
                    onEvent(RoutinesEvent.ToggleStep(routine.id, step.id))
                }
            }
            
            item { DailyInsightSmall() }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun CleanRitualStep(
    step: RoutineStep,
    onClick: () -> Unit
) {
    val isCompleted = step.isCompleted
    val backgroundColor = if (isCompleted) Color(0xFFE5E7E1) else Color.White // Muted Gray-Sage for completed
    val iconColor = if (isCompleted) Color(0xFF5A5F4B) else MaterialTheme.colorScheme.outlineVariant

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (!isCompleted) BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isCompleted) iconColor else Color.Transparent,
                shape = CircleShape,
                modifier = Modifier.size(24.dp).border(1.5.dp, iconColor, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.width(20.dp))
            
            Column {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(if (isCompleted) 0.6f else 1f)
                )
                Text(
                    text = "Gentle care for healthy skin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Spacer(Modifier.height(8.dp))
                
                Surface(
                    color = Color.Black.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "STEP ${step.layeringOrder + 1}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).alpha(0.4f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyInsightSmall() {
    Surface(
        color = Color(0xFFFDF0ED),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Daily Insight", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF8B5E3C))
            Text(
                text = "\"Patience is the foundation of every lasting ritual.\"",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Serif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
