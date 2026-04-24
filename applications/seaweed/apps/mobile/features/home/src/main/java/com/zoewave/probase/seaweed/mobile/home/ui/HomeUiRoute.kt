package com.zoewave.probase.seaweed.mobile.home.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.home.R
import com.zoewave.probase.seaweed.mobile.core.ui.components.CategoryBudgetProgressBar
import com.zoewave.probase.seaweed.mobile.core.ui.components.DonutChart
import com.zoewave.probase.seaweed.mobile.core.ui.components.FixedCostsSummaryCard
import com.zoewave.probase.seaweed.mobile.core.ui.components.RealMoneyHeroCard
import com.zoewave.probase.seaweed.mobile.core.ui.components.UnallocatedMoneyCard
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun HomeUiRoute(
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun HomeUiRoute(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
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
                    HomeExpandedContent(uiState, onEvent, navTo, padding)
                } else {
                    HomeCompactContent(uiState, onEvent, navTo, padding)
                }
            }
        }
    }
}

@Composable
private fun HomeExpandedContent(
    uiState: HomeUiState.Success,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    padding: PaddingValues
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            RealMoneyHeroCard(
                flexibleRemaining = uiState.flexibleMoneyRemaining,
                monthProgress = uiState.monthProgress,
                onAnalyticsClick = { navTo(SeaweedDestination.Analytics) }
            )
            OverviewSummaryCard(
                categories = uiState.categoriesSummary,
                navTo = navTo
            )
            UnallocatedMoneyCard(unallocatedAmount = uiState.unallocatedMoney)
            FixedCostsSummaryCard(
                totalFixedCosts = uiState.totalFixedCosts,
                income = uiState.monthlyIncome,
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
                    text = stringResource(CoreUiR.string.core_ui_view_all),
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
                        onDelete = { onEvent(HomeUiEvent.DeleteTransaction(transaction.id)) },
                        onClick = { navTo(SeaweedDestination.Transactions(category = null, transactionId = transaction.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCompactContent(
    uiState: HomeUiState.Success,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    padding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            RealMoneyHeroCard(
                flexibleRemaining = uiState.flexibleMoneyRemaining,
                monthProgress = uiState.monthProgress,
                onAnalyticsClick = { navTo(SeaweedDestination.Analytics) }
            )
        }
        item {
            FixedCostsSummaryCard(
                totalFixedCosts = uiState.totalFixedCosts,
                income = uiState.monthlyIncome,
                navTo = navTo
            )
        }
        item {
            OverviewSummaryCard(
                categories = uiState.categoriesSummary,
                navTo = navTo
            )
        }
        item {
            UnallocatedMoneyCard(unallocatedAmount = uiState.unallocatedMoney)
        }
        item {
            Button(
                onClick = { navTo(SeaweedDestination.CategoryGrid) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(CoreUiR.string.core_ui_all_categories))
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
                onDelete = { onEvent(HomeUiEvent.DeleteTransaction(transaction.id)) },
                onClick = { navTo(SeaweedDestination.Transactions(category = null, transactionId = transaction.id)) }
            )
        }
    }
}

@Composable
fun OverviewSummaryCard(
    categories: List<CategoryOverview>,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSpending = categories.sumOf { it.totalAmount }
    val totalTransactions = categories.sumOf { it.transactionCount }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(CoreUiR.string.core_ui_spending_summary),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(CoreUiR.string.core_ui_currency_format, String.format(Locale.getDefault(), "%.0f", totalSpending)),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(CoreUiR.string.core_ui_transactions_count, totalTransactions, categories.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        spendingByCategory = categories.associate { it.name to it.totalAmount },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.take(3).forEach { category ->
                    CategoryBudgetProgressBar(category = category)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            TextButton(
                onClick = { navTo(SeaweedDestination.Budget) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_manage_budgets))
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun HomeExpandedContentPreview() {
    MaterialTheme {
        HomeExpandedContent(
            uiState = HomeUiState.Success(
                transactions = listOf(Transaction("1", 42.0, "Food", "Lunch", 1000L)),
                categoriesSummary = listOf(CategoryOverview("Food", 42.0, 1, 100.0)),
                flexibleMoneyRemaining = 500.0,
                monthProgress = 0.5f
            ),
            onEvent = {},
            navTo = {},
            padding = PaddingValues(0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeCompactContentPreview() {
    MaterialTheme {
        HomeCompactContent(
            uiState = HomeUiState.Success(
                transactions = listOf(Transaction("1", 42.0, "Food", "Lunch", 1000L)),
                categoriesSummary = listOf(CategoryOverview("Food", 42.0, 1, 100.0)),
                flexibleMoneyRemaining = 500.0,
                monthProgress = 0.5f
            ),
            onEvent = {},
            navTo = {},
            padding = PaddingValues(0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OverviewSummaryCardPreview() {
    MaterialTheme {
        OverviewSummaryCard(
            categories = listOf(
                CategoryOverview("Food", 42.0, 1, 100.0),
                CategoryOverview("Coffee", 15.0, 1, 50.0)
            ),
            navTo = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeUiRoutePreview() {
    MaterialTheme {
        HomeUiRoute(
            uiState = HomeUiState.Success(
                transactions = listOf(
                    Transaction("1", 42.0, "Food", "Lunch", 1000L),
                    Transaction("2", 15.0, "Coffee", "Latte", 2000L)
                ),
                categoriesSummary = listOf(
                    CategoryOverview("Food", 42.0, 1, 100.0),
                    CategoryOverview("Coffee", 15.0, 1, 50.0)
                ),
                flexibleMoneyRemaining = 500.0,
                monthProgress = 0.5f
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Success(
                transactions = listOf(
                    Transaction("1", 42.0, "Food", "Lunch", 1000L),
                    Transaction("2", 15.0, "Coffee", "Latte", 2000L)
                ),
                categoriesSummary = listOf(
                    CategoryOverview("Food", 42.0, 1, 100.0),
                    CategoryOverview("Coffee", 15.0, 1, 50.0)
                ),
                flexibleMoneyRemaining = 500.0,
                monthProgress = 0.5f
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
