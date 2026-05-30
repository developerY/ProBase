package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.routines.R
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.RoutineTime

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
                        text = "Daily acts of mindful care.",
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
    val completedCount = routine.steps.count { it.isCompleted }
    val totalCount = routine.steps.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val accentColor = if (isMorning) Color(0xFF6B705C) else Color(0xFF457B9D)
    val bgBrush = if (isMorning) {
        Brush.verticalGradient(listOf(Color(0xFFF1F3F0), Color.White))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE9F5F9), Color.White))
    }

    Card(
        onClick = { navTo(KoColorRoute.RoutineDetail(routine.id)) },
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = if (isMorning) R.drawable.morning_routine_bg else R.drawable.night_routine_bg,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.45f),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            Box(modifier = Modifier.fillMaxSize().background(bgBrush, alpha = 0.3f)) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. TOP: Title
                    Text(
                        text = if (isMorning) "Morning Ritual" else "Evening Ritual",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    // 2. MIDDLE: Progress label and circle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isMorning) "CURRENT RITUAL" else "EVENING RITUAL",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.Black
                            )
                        }
                        
                        Box(
                            contentAlignment = Alignment.Center, 
                            modifier = Modifier
                                .size(84.dp)
                                .clickable(onClick = { onEvent(RoutinesEvent.ResetRoutine(routine.id)) })
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Black.copy(alpha = 0.05f),
                                strokeWidth = 6.dp
                            )
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                color = accentColor,
                                strokeWidth = 6.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$completedCount/$totalCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                                Text(
                                    text = "DONE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // 3. BOTTOM: Duration and rest
                    Column {
                        Text(
                            text = "15 mins duration",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = if (isMorning) "Prepare for a balanced day ahead." else "Every step is an act of self-love.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
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
