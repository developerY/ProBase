package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.ui.components.BarData
import com.zoewave.probase.core.ui.components.SimpleBarChart
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.SpendingPeriod
import com.zoewave.probase.seaweed.model.TrendPoint
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale

@Composable
fun AnalyticsUiRoute(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                AnalyticsUiEvent.OnBackClicked -> onBack()
            }
        },
        navTo = {}, // Placeholder if navigation from here is needed
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onEvent: (AnalyticsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Analytics") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AnalyticsUiEvent.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AnalyticsContent(
                uiState = uiState,
                onEvent = onEvent,
                navTo = navTo,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsContent(
    uiState: AnalyticsUiState,
    @Suppress("UnusedParameter") onEvent: (AnalyticsUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(SpendingPeriod.DAILY) }
    var selectedTrendPoint by remember { mutableStateOf<TrendPoint?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Spending Trends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        Row {
                            SpendingPeriod.entries.forEach { period ->
                                FilterChip(
                                    selected = selectedPeriod == period,
                                    onClick = { 
                                        selectedPeriod = period 
                                        selectedTrendPoint = null
                                    },
                                    label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val trendData = uiState.spendingTrends[selectedPeriod] ?: emptyList()
                    if (trendData.isNotEmpty()) {
                        SimpleBarChart(
                            data = trendData.map { BarData(it.label, it.value) },
                            onBarClick = { barData ->
                                selectedTrendPoint = trendData.find { it.label == barData.label }
                            },
                            selectedBar = selectedTrendPoint?.let { BarData(it.label, it.value) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("No data for this period")
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(visible = selectedTrendPoint != null) {
                selectedTrendPoint?.let { point ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Details for ${point.label}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total spent", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = String.format(Locale.getDefault(), "$%.2f", point.value),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Transactions", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${point.transactionCount}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            val topCategory = point.topCategory
                            if (topCategory != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Top Category", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = topCategory,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.habitInsights.isNotEmpty()) {
            item {
                Text("Spending Habits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            items(uiState.habitInsights) { insight ->
                HabitInsightCard(insight = insight)
            }
        }
    }
}

@Composable
private fun HabitInsightCard(insight: HabitInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(insight.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = insight.trendMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = String.format(Locale.getDefault(), "Total: $%.2f this month", insight.totalAmount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    MaterialTheme {
        AnalyticsScreen(
            uiState = AnalyticsUiState(
                isLoading = false,
                spendingTrends = mapOf(
                    SpendingPeriod.DAILY to listOf(
                        TrendPoint("Oct 01", 42.0, 1000L, 2, "Food"),
                        TrendPoint("Oct 02", 15.0, 2000L, 1, "Coffee")
                    )
                ),
                habitInsights = listOf(
                    HabitInsight("Coffee", 20, 80.0, 2.6, "You're doing this almost every day!")
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenLoadingPreview() {
    MaterialTheme {
        AnalyticsScreen(
            uiState = AnalyticsUiState(isLoading = true),
            onEvent = {},
            navTo = {}
        )
    }
}
