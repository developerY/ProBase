package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.ui.R as CoreUiR
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.Transaction
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun HomeUiRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == androidx.window.core.layout.WindowWidthSizeClass.EXPANDED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CoreUiR.string.core_ui_summary_title)) },
                actions = {
                    IconButton(onClick = { onEvent(HomeUiEvent.AddRandomTransaction) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(CoreUiR.string.core_ui_add_random_data))
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        when (uiState) {
            HomeUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            BalanceCard(balance = uiState.totalBalance)
                            OverviewSummaryCard(categories = uiState.categoriesSummary)
                            CategoryQuickJumpRow(
                                categories = uiState.categoriesSummary,
                                navTo = navTo
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(CoreUiR.string.core_ui_recent_transactions),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { navTo(SeaweedDestination.Transactions(null)) }
                                )
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.transactions.take(10), key = { it.id }) { transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        onDelete = { onEvent(HomeUiEvent.DeleteTransaction(transaction.id)) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            BalanceCard(balance = uiState.totalBalance)
                        }
                        item {
                            OverviewSummaryCard(categories = uiState.categoriesSummary)
                        }
                        item {
                            CategoryQuickJumpRow(
                                categories = uiState.categoriesSummary,
                                navTo = navTo
                            )
                        }
                        item {
                            Button(
                                onClick = { navTo(SeaweedDestination.CategoryGrid) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View All Categories")
                            }
                        }
                        item {
                            Text(
                                text = stringResource(CoreUiR.string.core_ui_recent_transactions),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(uiState.transactions.take(5), key = { it.id }) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onDelete = { onEvent(HomeUiEvent.DeleteTransaction(transaction.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = stringResource(CoreUiR.string.core_ui_total_balance), style = MaterialTheme.typography.labelLarge)
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", balance)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun OverviewSummaryCard(
    categories: List<CategoryOverview>,
    modifier: Modifier = Modifier
) {
    val totalSpending = categories.sumOf { it.totalAmount }
    val totalTransactions = categories.sumOf { it.transactionCount }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Spending Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.0f", totalSpending)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$totalTransactions transactions across ${categories.size} categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    spendingByCategory = categories.associate { it.name to it.totalAmount },
                    modifier = Modifier.fillMaxSize()
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${categories.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryQuickJumpRow(
    categories: List<CategoryOverview>,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val topCategories = remember(categories) {
        categories.take(5)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Jump",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(topCategories) { category ->
                CategoryQuickJumpCard(
                    category = category,
                    onClick = { navTo(SeaweedDestination.Transactions(category.name)) }
                )
            }
        }
    }
}

@Composable
fun CategoryQuickJumpCard(
    category: CategoryOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorIndex = category.name.hashCode().absoluteValue % categoryColors.size
    val color = categoryColors[colorIndex]

    Card(
        onClick = onClick,
        modifier = modifier
            .width(130.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.0f", category.totalAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { 1f }, // Placeholder
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    spendingByCategory: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalSpending = spendingByCategory.values.sum()
    if (totalSpending == 0.0) return

    val proportions = spendingByCategory.values.map { (it / totalSpending).toFloat() }
    
    Canvas(modifier = modifier) {
        val strokeWidth = 24f
        
        // Background ring
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.2f),
            style = Stroke(width = strokeWidth)
        )
        
        var startAngle = -90f
        spendingByCategory.keys.forEachIndexed { index, category ->
            val amount = spendingByCategory[category] ?: 0.0
            val sweepAngle = (amount / totalSpending).toFloat() * 360f
            
            if (sweepAngle > 0) {
                drawArc(
                    color = categoryColors[index % categoryColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            startAngle += sweepAngle
        }
    }
}

private val categoryColors = listOf(
    Color(0xFF6750A4), // Purple
    Color(0xFF006C4C), // Green
    Color(0xFFB3261E), // Red
    Color(0xFF625B71), // Muted Purple
    Color(0xFF7D5260), // Muted Red
    Color(0xFF006A6A)  // Teal
)
