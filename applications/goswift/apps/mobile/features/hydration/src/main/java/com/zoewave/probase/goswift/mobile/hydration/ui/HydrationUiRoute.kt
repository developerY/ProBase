package com.zoewave.probase.goswift.mobile.hydration.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.goswift.features.main.navigation.GoSwiftDestination
import com.zoewave.probase.goswift.mobile.hydration.ui.components.WavyWaterLevel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HydrationUiRoute(
    navTo: (GoSwiftDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HydrationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HydrationUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun HydrationUiRoute(
    uiState: HydrationUiState,
    onEvent: (HydrationUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (GoSwiftDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    HydrationScreen(
        uiState = uiState,
        onEvent = onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(
    uiState: HydrationUiState,
    onEvent: (HydrationUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Intelligent Hydration") })
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HydrationProgressCard(uiState.dailyTotalLiters, uiState.targetLiters)
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Quick Log", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HydrationQuickLogButton("250ml", 0.25) { onEvent(HydrationUiEvent.AddWater(0.25)) }
                        HydrationQuickLogButton("500ml", 0.5) { onEvent(HydrationUiEvent.AddWater(0.5)) }
                        HydrationQuickLogButton("750ml", 0.75) { onEvent(HydrationUiEvent.AddWater(0.75)) }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Recent Logs", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                    Spacer(Modifier.height(8.dp))
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
fun HydrationProgressCard(current: Double, target: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WavyWaterLevel(
                progress = (current / target).toFloat(),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text("Daily Progress", style = MaterialTheme.typography.labelLarge)
            Text(String.format("%.2fL / %.2fL", current, target), style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
fun HydrationQuickLogButton(label: String, amount: Double, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(label)
    }
}

@Composable
fun HydrationLogItem(log: HydrationLog) {
    val time = Instant.ofEpochMilli(log.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
        
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${(log.amountLiters * 1000).toInt()} ml", style = MaterialTheme.typography.bodyLarge)
            Text(time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}
