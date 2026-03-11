package com.zoewave.probase.features.health.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.zoewave.probase.features.health.R
import com.zoewave.probase.features.health.ui.HealthEvent
import com.zoewave.probase.features.health.ui.HealthUiState
import com.zoewave.probase.features.health.ui.components.charts.GenericWeeklyChart
import com.zoewave.probase.features.health.ui.sessions.SessionCard
import com.zoewave.probase.features.health.ui.sessions.SessionDetailDialog
import com.zoewave.probase.features.health.ui.settings.HealthConnectionStatus

private enum class HealthTabOverView(val labelRes: Int, val icon: ImageVector) {
    Settings(R.string.features_health_health_route_enable_health_connect_title, Icons.Default.Settings),
    Data(R.string.features_health_overview_label, Icons.Default.DateRange),
    Sessions(R.string.features_health_sessions_label, Icons.Default.List)
}

@Composable
fun HealthDashboardOverView(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(HealthTabOverView.Data) }

    // 1. State to track which session is clicked (null = no dialog)
    var selectedSession by remember { mutableStateOf<ExerciseSessionRecord?>(null) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                HealthTabOverView.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (currentTab) {
                HealthTabOverView.Settings -> { /* ... same as before ... */
                    HealthConnectionStatus(onEvent = onEvent)
                }

                HealthTabOverView.Data -> { /* ... same as before ... */
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        GenericWeeklyChart(
                            stringResource(R.string.features_health_steps_label),
                            state.weeklySteps.mapValues { it.value.toDouble() },
                            MaterialTheme.colorScheme.primary
                        ) { "${it.toInt()}" }
                        GenericWeeklyChart(
                            stringResource(R.string.features_health_calories_label),
                            state.weeklyCalories,
                            Color(0xFFFF9800)
                        ) { "${it.toInt()}" }
                        GenericWeeklyChart(
                            stringResource(R.string.features_health_distance_label),
                            state.weeklyDistance,
                            Color(0xFF03A9F4)
                        ) { String.format("%.1f", it / 1000) }
                    }
                }

                HealthTabOverView.Sessions -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(R.string.features_health_recent_sessions_title),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (state.sessions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.features_health_no_recent_activities), color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.sessions) { session ->
                                    SessionCard(
                                        session = session,
                                        onClick = { selectedSession = session } // <--- Handle Click
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Show Dialog if a session is selected
        if (selectedSession != null) {
            SessionDetailDialog(
                session = selectedSession!!,
                onDismiss = { selectedSession = null },
                onDelete = { uid ->
                    onEvent(HealthEvent.DeleteSession(uid)) // <--- Trigger Event
                }
            )
        }

        if (selectedSession != null) {
            SessionDetailDialog(
                session = selectedSession!!,
                onDismiss = { selectedSession = null },
                onDelete = { uid ->
                    onEvent(HealthEvent.DeleteSession(uid)) // <--- Trigger Event
                }
            )
        }
    }
}

// --- Clickable Card ---


// --- The Detail Dialog ---

