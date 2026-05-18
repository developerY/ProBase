package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = { Text("GLOW RITUALS", style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* History */ }) { Icon(Icons.Default.History, null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Bio-Synced Beauty.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rituals curated for your circadian rhythm.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            item {
                uiState.morningRoutine?.let { routine ->
                    HeroRitualCard(
                        routine = routine,
                        onClick = { navTo(KoColorRoute.RoutineDetail(routine.id)) }
                    )
                }
            }
            
            item {
                uiState.eveningRoutine?.let { routine ->
                    HeroRitualCard(
                        routine = routine,
                        onClick = { navTo(KoColorRoute.RoutineDetail(routine.id)) }
                    )
                }
            }

            item {
                SectionTitle(title = "Ritual Performance", subtitle = "Monthly consistency")
                RitualAnalyticsCard()
            }
        }
    }
}

@Composable
private fun HeroRitualCard(
    routine: BeautyRoutine,
    onClick: () -> Unit
) {
    val isMorning = routine.time == RoutineTime.MORNING
    val accentColor = if (isMorning) Color(0xFFFFB74D) else Color(0xFF7986CB)
    val bgBrush = if (isMorning) {
        Brush.verticalGradient(listOf(Color(0xFFFFF9C4).copy(alpha = 0.4f), Color.White))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE8EAF6).copy(alpha = 0.4f), Color.White))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(bgBrush).padding(32.dp)) {
            // Ambient Watermark
            Icon(
                imageVector = if (isMorning) Icons.Default.LightMode else Icons.Default.NightsStay,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 24.dp)
                    .size(180.dp)
                    .alpha(0.05f),
                tint = accentColor
            )

            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
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

                    // Progress Ring
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = accentColor.copy(alpha = 0.1f),
                            strokeWidth = 4.dp
                        )
                        val completedCount = routine.steps.count { it.isCompleted }
                        val totalCount = routine.steps.size
                        val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = accentColor,
                            strokeWidth = 4.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
                
                Column {
                    Text(
                        text = routine.time.biologicalObjective.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (isMorning) "The Radiance Ritual" else "The Deep Restoration",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = routine.time.objectiveDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun RitualAnalyticsCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "94%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "CONSISTENCY SCORE", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.alpha(0.5f)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(7) { i ->
                    Box(
                        modifier = Modifier
                            .size(height = 40.dp, width = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < 6) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
