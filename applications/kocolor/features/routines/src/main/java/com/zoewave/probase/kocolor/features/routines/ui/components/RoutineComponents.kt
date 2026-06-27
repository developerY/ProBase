package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.RoutineTime
import com.zoewave.probase.kocolor.features.routines.R
import com.zoewave.probase.kocolor.features.routines.ui.RoutinesEvent
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun HeroRitualCard(
    uiState: BeautyRoutine,
    modifier: Modifier = Modifier,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val routine = uiState
    val completedCount = routine.steps.count { it.isCompleted }
    val totalCount = routine.steps.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val title = when (routine.time) {
        RoutineTime.MORNING -> stringResource(R.string.applications_kocolor_features_routines_morning_ritual)
        RoutineTime.MEALS -> stringResource(R.string.applications_kocolor_features_routines_meals_ritual)
        RoutineTime.EVENING -> stringResource(R.string.applications_kocolor_features_routines_evening_ritual)
        else -> routine.title
    }

    val label = when (routine.time) {
        RoutineTime.MORNING -> stringResource(R.string.applications_kocolor_features_routines_current_ritual)
        RoutineTime.MEALS -> stringResource(R.string.applications_kocolor_features_routines_bio_sync_ritual)
        RoutineTime.EVENING -> stringResource(R.string.applications_kocolor_features_routines_evening_ritual_label)
        else -> stringResource(R.string.applications_kocolor_features_routines_ritual_label)
    }

    val description = when (routine.time) {
        RoutineTime.MORNING -> stringResource(R.string.applications_kocolor_features_routines_morning_desc)
        RoutineTime.MEALS -> stringResource(R.string.applications_kocolor_features_routines_meals_desc)
        RoutineTime.EVENING -> stringResource(R.string.applications_kocolor_features_routines_evening_desc)
        else -> ""
    }

    val accentColor = when (routine.time) {
        RoutineTime.MORNING -> Color(0xFF6B705C)
        RoutineTime.MEALS -> Color(0xFFE0C097)
        RoutineTime.EVENING -> Color(0xFF457B9D)
        else -> Color.Gray
    }

    val bgBrush = when (routine.time) {
        RoutineTime.MORNING -> Brush.verticalGradient(listOf(Color(0xFFF1F3F0), Color.White))
        RoutineTime.MEALS -> Brush.verticalGradient(listOf(Color(0xFFF9F7F2), Color.White))
        RoutineTime.EVENING -> Brush.verticalGradient(listOf(Color(0xFFE9F5F9), Color.White))
        else -> Brush.verticalGradient(listOf(Color.LightGray, Color.White))
    }

    Card(
        onClick = { navTo(KoColorRoute.RoutineDetail(routine.id)) },
        modifier = modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = when (routine.time) {
                    RoutineTime.MORNING -> R.drawable.morning_routine_bg
                    RoutineTime.MEALS -> R.drawable.meals_ritual_bg
                    else -> R.drawable.night_routine_bg
                },
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.35f),
                contentScale = ContentScale.Crop
            )
            
            Box(modifier = Modifier.fillMaxSize().background(bgBrush, alpha = 0.7f)) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
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
                                text = label,
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
                                    text = stringResource(R.string.applications_kocolor_features_routines_progress_format, completedCount, totalCount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_routines_done),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_routines_duration_format, 15),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyInsightBanner(
    uiState: Unit,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Surface(
        color = Color(0xFFFDF0ED),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.applications_kocolor_features_routines_daily_insight),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B5E3C),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.applications_kocolor_features_routines_insight_quote),
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
                Text(stringResource(R.string.applications_kocolor_features_routines_view_progress), fontWeight = FontWeight.Bold)
            }
        }
    }
}
