package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.bills.ui.BillsScreen
import com.zoewave.probase.seaweed.mobile.bills.ui.BillsViewModel
import com.zoewave.probase.core.ui.components.BarData
import com.zoewave.probase.core.ui.components.SimpleBarChart
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem
import com.zoewave.probase.seaweed.model.HabitInsight
import com.zoewave.probase.seaweed.model.SpendingPeriod
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.model.navigation.TransactionTab
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionsUiRoute(
    modifier: Modifier = Modifier,
    initialCategory: String? = null,
    initialTransactionId: String? = null,
    initialTab: TransactionTab = TransactionTab.RECENT,
    viewModel: TransactionsViewModel = hiltViewModel(),
    billsViewModel: BillsViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val billsUiState by billsViewModel.uiState.collectAsStateWithLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialCategory, initialTransactionId, initialTab) {
        viewModel.setInitialCategory(initialCategory)
        viewModel.setInitialTab(initialTab)
        if (initialTransactionId != null) {
            viewModel.onEvent(TransactionsUiEvent.SelectTransaction(initialTransactionId))
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, initialTransactionId)
        }
    }

    BackHandler(navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            TransactionsListPane(
                uiState = uiState,
                billsUiState = billsUiState,
                onEvent = viewModel::onEvent,
                onBillsEvent = billsViewModel::onEvent,
                navTo = navTo,
                onTransactionClick = { id ->
                    viewModel.onEvent(TransactionsUiEvent.SelectTransaction(id))
                    scope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                }
            )
        },
        detailPane = {
            TransactionDetailPane(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onBack = {
                    if (navigator.canNavigateBack()) {
                        scope.launch {
                            navigator.navigateBack()
                        }
                    }
                }
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListPane(
    uiState: TransactionsUiState,
    @Suppress("UnusedParameter") billsUiState: Any, // We need the actual BillsUiState
    onEvent: (TransactionsUiEvent) -> Unit,
    onBillsEvent: (com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    onTransactionClick: (String) -> Unit
) {
    // Re-cast for proper usage, though it's better to pass it in directly
    val billsState = billsUiState as? com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { navTo(SeaweedDestination.Budget) }) {
                        Icon(Icons.Default.PieChart, contentDescription = "Budget")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is TransactionsUiState.Success && uiState.selectedTab == TransactionTab.RECENT) {
                FloatingActionButton(onClick = { navTo(SeaweedDestination.AddTransaction) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            } else if (uiState is TransactionsUiState.Success && uiState.selectedTab == TransactionTab.CYCLIC) {
                FloatingActionButton(onClick = { /* TODO: Show Add Bill Dialog */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bill")
                }
            }
        }
    ) { padding ->
        when (uiState) {
            TransactionsUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TransactionsUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    PrimaryTabRow(
                        selectedTabIndex = uiState.selectedTab.ordinal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = uiState.selectedTab == TransactionTab.RECENT,
                            onClick = { onEvent(TransactionsUiEvent.SelectTab(TransactionTab.RECENT)) },
                            text = { Text("Recent") }
                        )
                        Tab(
                            selected = uiState.selectedTab == TransactionTab.CYCLIC,
                            onClick = { onEvent(TransactionsUiEvent.SelectTab(TransactionTab.CYCLIC)) },
                            text = { Text("Cyclic") }
                        )
                        Tab(
                            selected = uiState.selectedTab == TransactionTab.ANALYTICS,
                            onClick = { onEvent(TransactionsUiEvent.SelectTab(TransactionTab.ANALYTICS)) },
                            text = { Text("Analytics") }
                        )
                    }

                    when (uiState.selectedTab) {
                        TransactionTab.RECENT -> {
                            RecentTransactionsContent(
                                uiState = uiState,
                                onEvent = onEvent,
                                onTransactionClick = onTransactionClick
                            )
                        }
                        TransactionTab.CYCLIC -> {
                            if (billsState != null) {
                                BillsScreen(
                                    uiState = billsState,
                                    onEvent = onBillsEvent,
                                    navTo = navTo,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        TransactionTab.ANALYTICS -> {
                            AnalyticsPane(
                                spendingTrends = uiState.spendingTrends,
                                habitInsights = uiState.habitInsights,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsContent(
    uiState: TransactionsUiState.Success,
    onEvent: (TransactionsUiEvent) -> Unit,
    onTransactionClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CategoryFilterRow(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            onSelect = { onEvent(TransactionsUiEvent.SelectCategory(it)) }
        )
        if (uiState.filteredTransactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredTransactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { onEvent(TransactionsUiEvent.DeleteTransaction(transaction.id)) },
                        onClick = { onTransactionClick(transaction.id) },
                        isSelected = uiState.selectedTransactionId == transaction.id
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailPane(
    uiState: TransactionsUiState,
    onEvent: (TransactionsUiEvent) -> Unit,
    onBack: () -> Unit
) {
    val transaction = (uiState as? TransactionsUiState.Success)?.selectedTransaction

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (transaction != null) {
                        IconButton(onClick = { 
                            onEvent(TransactionsUiEvent.DeleteTransaction(transaction.id))
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (transaction != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Amount", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (transaction.amount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Category", style = MaterialTheme.typography.labelSmall)
                            Text(transaction.category, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Date", style = MaterialTheme.typography.labelSmall)
                            val dateString = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.date)
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Select a transaction to see details")
            }
        }
    }
}

@Composable
fun AnalyticsPane(
    spendingTrends: Map<SpendingPeriod, List<com.zoewave.probase.seaweed.model.TrendPoint>>,
    habitInsights: List<HabitInsight>,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(SpendingPeriod.DAILY) }

    LazyColumn(
        modifier = modifier,
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
                                    onClick = { selectedPeriod = period },
                                    label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val trendData = spendingTrends[selectedPeriod] ?: emptyList()
                    if (trendData.isNotEmpty()) {
                        SimpleBarChart(
                            data = trendData.map { BarData(it.label, it.value) },
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

        if (habitInsights.isNotEmpty()) {
            item {
                Text("Spending Habits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            items(habitInsights) { insight ->
                HabitInsightCard(insight = insight)
            }
        }
    }
}

@Composable
fun HabitInsightCard(insight: HabitInsight) {
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
                    .clip(androidx.compose.foundation.shape.CircleShape)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelect(null) },
                label = { Text("All") }
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelect(category) },
                label = { Text(category) }
            )
        }
    }
}
