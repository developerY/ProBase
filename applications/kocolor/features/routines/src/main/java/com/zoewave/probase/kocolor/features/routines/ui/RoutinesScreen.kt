package com.zoewave.probase.kocolor.features.routines.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.routines.R
import com.zoewave.probase.kocolor.features.routines.ui.components.DailyInsightBanner
import com.zoewave.probase.kocolor.features.routines.ui.components.HeroRitualCard
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.core.model.ritual.RoutineTime

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
    modifier: Modifier = Modifier,
    onEvent: (RoutinesEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_routines_title), style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Home) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_routines_back))
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val currentActiveTime = when {
                hour in 5..9 -> RoutineTime.MORNING
                hour in 10..19 -> RoutineTime.MEALS
                else -> RoutineTime.EVENING
            }

            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_routines_serene_rituals),
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_routines_serene_rituals_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            item {
                uiState.morningRoutine?.let { routine ->
                    HeroRitualCard(
                        uiState = routine,
                        isActive = currentActiveTime == RoutineTime.MORNING,
                        onEvent = { onEvent(RoutinesEvent.ResetRoutine(routine.id)) },
                        navTo = navTo
                    )
                }
            }

            item {
                uiState.mealsRoutine?.let { routine ->
                    HeroRitualCard(
                        uiState = routine,
                        isActive = currentActiveTime == RoutineTime.MEALS,
                        onEvent = { onEvent(RoutinesEvent.ResetRoutine(routine.id)) },
                        navTo = navTo
                    )
                }
            }

            item {
                uiState.eveningRoutine?.let { routine ->
                    HeroRitualCard(
                        uiState = routine,
                        isActive = currentActiveTime == RoutineTime.EVENING,
                        onEvent = { onEvent(RoutinesEvent.ResetRoutine(routine.id)) },
                        navTo = navTo
                    )
                }
            }

            item {
                DailyInsightBanner(uiState = Unit, onEvent = {}, navTo = navTo)
            }
        }
    }
}
