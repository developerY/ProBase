package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.zoewave.probase.kocolor.features.routines.ui.components.GlassConnectionHeaderAction
import com.zoewave.probase.kocolor.model.*

@Preview(showBackground = true)
@Composable
private fun RoutinesScreenPreview() {
    MaterialTheme {
        RoutinesScreen(
            uiState = RoutinesUiState(
                morningRoutine = BeautyRoutine(title = "Morning beautiful routine", time = RoutineTime.MORNING, steps = emptyList(), date = 0),
                eveningRoutine = BeautyRoutine(title = "Evening restoration", time = RoutineTime.EVENING, steps = emptyList(), date = 0)
            ),
            onEvent = {},
            navTo = {}
        )
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
                title = { Text("GLOW RITUALS", style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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
                        text = "Serene Rituals.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your daily acts of mindful care.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            item {
                uiState.morningRoutine?.let { routine ->
                    HeroRitualCard(
                        uiState = routine,
                        onEvent = { onEvent(RoutinesEvent.ResetRoutine(routine.id)) },
                        navTo = navTo
                    )
                }
            }

            item {
                uiState.eveningRoutine?.let { routine ->
                    HeroRitualCard(
                        uiState = routine,
                        onEvent = { onEvent(RoutinesEvent.ResetRoutine(routine.id)) },
                        navTo = navTo
                    )
                }
            }

            item {
                DailyInsightBanner(uiState = Unit, onEvent = {}, navTo = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeroRitualCardPreview() {
    MaterialTheme {
        HeroRitualCard(
            uiState = BeautyRoutine(title = "Morning", time = RoutineTime.MORNING, steps = emptyList(), date = 0),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun HeroRitualCard(
    uiState: BeautyRoutine,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val routine = uiState
    val isMorning = routine.time == RoutineTime.MORNING
    val accentColor = if (isMorning) Color(0xFF6B705C) else Color(0xFF457B9D) // Sage vs Muted Blue
    val bgBrush = if (isMorning) {
        Brush.verticalGradient(listOf(Color(0xFFF1F3F0), Color.White)) // Pale Sage
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE9F5F9), Color.White)) // Pale Blue
    }

    Card(
        onClick = { navTo(KoColorRoute.RoutineDetail(routine.id)) },
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(bgBrush).padding(24.dp)) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMorning) Icons.Default.LightMode else Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isMorning) "CURRENT RITUAL" else "EVENING RITUAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                        Text(
                            text = if (isMorning) "Your Morning Ritual" else "Your Evening Ritual",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Minimal Circular Progress with Reset Trigger
                    Box(
                        contentAlignment = Alignment.Center, 
                        modifier = Modifier
                            .size(54.dp)
                            .clickable(onClick = { onEvent(RoutinesEvent.ResetRoutine(routine.id)) })
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = accentColor.copy(alpha = 0.1f),
                            strokeWidth = 3.dp
                        )
                        val completedCount = routine.steps.count { it.isCompleted }
                        val totalCount = routine.steps.size
                        val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                        
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = accentColor,
                            strokeWidth = 3.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        
                        // Show Refresh icon if progress is high, otherwise fraction
                        if (progress > 0.99f && totalCount > 0) {
                            Icon(Icons.Default.Refresh, null, tint = accentColor, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                text = "$completedCount/$totalCount",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (isMorning) "Prepare for a balanced day ahead." 
                               else "Every step is an act of self-love.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyInsightBannerPreview() {
    MaterialTheme {
        DailyInsightBanner(uiState = Unit, onEvent = {}, navTo = {})
    }
}

@Composable
fun DailyInsightBanner(
    uiState: Unit,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Surface(
        color = Color(0xFFFDF0ED), // Soft Peach
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Daily Insight",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B5E3C),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "\"Patience is the foundation of every lasting ritual.\"",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color(0xFF5D4037)
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { /* View Progress */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B5E57)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("VIEW PROGRESS", fontWeight = FontWeight.Bold)
            }
        }
    }
}
