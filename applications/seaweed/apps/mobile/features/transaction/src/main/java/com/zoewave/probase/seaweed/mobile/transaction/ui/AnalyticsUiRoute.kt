package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.ui.components.BarData
import com.zoewave.probase.core.ui.components.SimpleBarChart
import com.zoewave.probase.seaweed.mobile.transaction.R
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.SpendingHeatmap
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.SpendingPeriod
import com.zoewave.probase.seaweed.model.TrendPoint
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun AnalyticsUiRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsUiRoute(
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

@Composable
internal fun AnalyticsUiRoute(
    uiState: AnalyticsUiState,
    onEvent: (AnalyticsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    AnalyticsScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
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
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_analytics_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AnalyticsUiEvent.OnBackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(CoreUiR.string.cd_navigate_back)
                        )
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
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    var selectedPeriod by remember { mutableStateOf(SpendingPeriod.DAILY) }
    var selectedTrendPoint by remember { mutableStateOf<TrendPoint?>(null) }
    var selectedHeatmapDate by remember { mutableStateOf<LocalDate?>(null) }
    var isHabitsExpanded by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val maxSpending = remember(uiState.heatmapData) {
        val maxVal = if (uiState.heatmapData.isEmpty()) 0L else uiState.heatmapData.values.max()
        maxVal.toDouble()
    }

    val filteredTransactions = remember(selectedCategory, uiState.allTransactions) {
        if (selectedCategory == null) uiState.allTransactions
        else {
            val catId = uiState.categoriesMap.values.find { it.name == selectedCategory }?.id
            uiState.allTransactions.filter { it.categoryId == catId }
        }
    }

    val trends = remember(filteredTransactions, selectedCategory) {
        viewModel.calculateTrendsForTransactions(filteredTransactions, uiState.categoriesMap)
    }

    val heatmapData = remember(filteredTransactions, selectedCategory) {
        viewModel.calculateHeatmapDataForTransactions(filteredTransactions)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (uiState.habitInsights.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_spending_habits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedCategory != null) {
                            Text(
                                text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_filtering_by, selectedCategory!!),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { isHabitsExpanded = !isHabitsExpanded }) {
                        Icon(
                            imageVector = if (isHabitsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isHabitsExpanded) 
                                stringResource(CoreUiR.string.action_collapse) 
                            else 
                                stringResource(CoreUiR.string.action_expand)
                        )
                    }
                }
            }
            
            if (isHabitsExpanded) {
                items(uiState.habitInsights) { insight ->
                    HabitInsightCard(
                        insight = insight,
                        isSelected = selectedCategory == insight.category,
                        onClick = {
                            selectedCategory = if (selectedCategory == insight.category) null else insight.category
                            selectedTrendPoint = null
                            selectedHeatmapDate = null
                        }
                    )
                }
            }
        }

        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider() }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_trends)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_heatmap)) }
                )
            }
        }

        if (selectedTabIndex == 0) {
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
                            Text(
                                text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_summary), 
                                style = MaterialTheme.typography.titleSmall, 
                                fontWeight = FontWeight.SemiBold
                            )
                            
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
                        
                        val trendData = trends[selectedPeriod] ?: emptyList()
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
                                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_no_data_period))
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
                                    text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_details_for, point.label),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_total_spent), 
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_currency_format, String.format(Locale.getDefault(), "%.2f", point.value)),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_transactions), 
                                            style = MaterialTheme.typography.labelSmall
                                        )
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
                                    Text(
                                        text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_top_category), 
                                        style = MaterialTheme.typography.labelSmall
                                    )
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
        }

        if (selectedTabIndex == 1) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_spending_heatmap),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    SpendingHeatmap(
                        heatmapData = heatmapData,
                        selectedDate = selectedHeatmapDate,
                        onDayClick = { selectedHeatmapDate = it }
                    )
                }
            }

            item {
                AnimatedVisibility(visible = selectedHeatmapDate != null) {
                    val zoneId = ZoneId.systemDefault()
                    val dayTransactions = filteredTransactions.filter {
                        it.amountCents < 0 && Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() == selectedHeatmapDate
                    }
                    
                    Column {
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_transactions_for, selectedHeatmapDate?.toString() ?: ""),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (dayTransactions.isEmpty()) {
                            Text(
                                text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_no_transactions_day), 
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            dayTransactions.forEach { transaction ->
                                val categoryName = uiState.categoriesMap[transaction.categoryId]?.name ?: transaction.categoryId
                                TransactionItem(
                                    transaction = transaction,
                                    categoryName = categoryName,
                                    onDelete = {},
                                    onClick = {}
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        /* Spending Habits moved to the top */
    }
}

@Composable
private fun HabitInsightCard(
    insight: HabitInsight,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CardDefaults.shape
            ),
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
                
                val budgetLimit = insight.budgetLimit
                if (budgetLimit != null) {
                    val progress = (insight.totalAmount / budgetLimit).toFloat()
                    val progressColor = if (progress > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = progressColor,
                            trackColor = progressColor.copy(alpha = 0.2f)
                        )
                        Text(
                            text = stringResource(
                                R.string.applications_seaweed_apps_mobile_features_transaction_budget_of, 
                                String.format(Locale.getDefault(), "$%.2f", insight.totalAmount),
                                String.format(Locale.getDefault(), "$%.0f", budgetLimit)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (progress > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = stringResource(
                            R.string.applications_seaweed_apps_mobile_features_transaction_total_this_month, 
                            String.format(Locale.getDefault(), "$%.2f", insight.totalAmount)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitInsightCardPreview() {
    MaterialTheme {
        HabitInsightCard(
            insight = HabitInsight("Coffee", 20, 80.0, 2.6, "You're doing this almost every day!"),
            isSelected = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsUiRoutePreview() {
    MaterialTheme {
        AnalyticsUiRoute(
            uiState = AnalyticsUiState(
                isLoading = false,
                spendingTrends = mapOf(
                    SpendingPeriod.DAILY to listOf(
                        TrendPoint("Oct 01", 42.0, 1000L, 2, "Food")
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
