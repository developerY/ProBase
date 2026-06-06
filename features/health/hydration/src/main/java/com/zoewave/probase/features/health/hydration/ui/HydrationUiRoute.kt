package com.zoewave.probase.features.health.hydration.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.health.hydration.ui.components.HydrationGlassCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HydrationUiRoute(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HydrationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HydrationUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier
    )
}

@Composable
internal fun HydrationUiRoute(
    uiState: HydrationUiState,
    onEvent: (HydrationUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HydrationScreen(
        uiState = uiState,
        onEvent = onEvent,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(
    uiState: HydrationUiState,
    onEvent: (HydrationUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hydration Tracking") })
        },
        modifier = modifier
    ) { padding ->
        when (uiState) {
            HydrationUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HydrationUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    HydrationGlassCard(
                        current = uiState.dailyTotalLiters,
                        goal = uiState.targetLiters,
                        onAdd = { onEvent(HydrationUiEvent.AddWater(it)) },
                        onNavigateToSettings = onNavigateToSettings
                    )
                    
                    Text(
                        text = "RECENT LOGS", 
                        style = MaterialTheme.typography.labelMedium, 
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.recentLogs) { log ->
                            HydrationLogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HydrationLogItem(log: HydrationLog) {
    val time = Instant.ofEpochMilli(log.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
        
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(log.amountLiters * 1000).toInt()} ml", 
                style = MaterialTheme.typography.bodyLarge
            )
            Text(time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}
