package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.bills.ui.BillsScreen
import com.zoewave.probase.seaweed.mobile.bills.ui.BillsViewModel
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.model.navigation.TransactionTab
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionsUiRoute(
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: String? = null,
    initialTransactionId: String? = null,
    initialTab: TransactionTab = TransactionTab.RECENT,
    viewModel: TransactionsViewModel = hiltViewModel(),
    billsViewModel: BillsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val billsUiState by billsViewModel.uiState.collectAsStateWithLifecycle()

    TransactionsUiRoute(
        uiState = uiState,
        billsUiState = billsUiState,
        onEvent = { event ->
            // Navigation handled in the caller or passed down
            viewModel.onEvent(event)
        },
        navTo = navTo,
        modifier = modifier,
        initialCategory = initialCategory,
        initialTransactionId = initialTransactionId,
        initialTab = initialTab
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionsUiRoute(
    uiState: TransactionsUiState,
    billsUiState: com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState,
    onEvent: (TransactionsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: String? = null,
    initialTransactionId: String? = null,
    initialTab: TransactionTab = TransactionTab.RECENT,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialCategory, initialTransactionId, initialTab) {
        // These might need to be passed as events instead of being handled here
        // But for now we keep the logic similar
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
                onEvent = { event ->
                    when (event) {
                        is TransactionsUiEvent.NavigateTo -> navTo(event.destination)
                        TransactionsUiEvent.OnBack -> { /* Handle back if needed */ }
                        is TransactionsUiEvent.SelectTransaction -> {
                            onEvent(event)
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, event.id)
                            }
                        }
                        else -> onEvent(event)
                    }
                },
                navTo = navTo
            )
        },
        detailPane = {
            TransactionDetailPane(
                uiState = uiState,
                onEvent = { event ->
                    when (event) {
                        TransactionsUiEvent.OnBack -> {
                            if (navigator.canNavigateBack()) {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            }
                        }
                        is TransactionsUiEvent.NavigateTo -> navTo(event.destination)
                        else -> onEvent(event)
                    }
                },
                navTo = navTo
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListPane(
    uiState: TransactionsUiState,
    billsUiState: com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState,
    onEvent: (TransactionsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { onEvent(TransactionsUiEvent.NavigateTo(SeaweedDestination.Analytics)) }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Analytics")
                    }
                    IconButton(onClick = { onEvent(TransactionsUiEvent.NavigateTo(SeaweedDestination.Budget)) }) {
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
                    }

                    when (uiState.selectedTab) {
                        TransactionTab.RECENT -> {
                            RecentTransactionsContent(
                                uiState = uiState,
                                onEvent = onEvent
                            )
                        }
                        TransactionTab.CYCLIC -> {
                            BillsScreen(
                                uiState = billsUiState,
                                onEvent = { /* Map to bills event if needed */ },
                                navTo = navTo,
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
private fun RecentTransactionsContent(
    uiState: TransactionsUiState.Success,
    onEvent: (TransactionsUiEvent) -> Unit
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
                        onClick = { onEvent(TransactionsUiEvent.SelectTransaction(transaction.id)) },
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
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit
) {
    val transaction = (uiState as? TransactionsUiState.Success)?.selectedTransaction

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(TransactionsUiEvent.OnBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (transaction != null) {
                        IconButton(onClick = { 
                            onEvent(TransactionsUiEvent.DeleteTransaction(transaction.id))
                            onEvent(TransactionsUiEvent.OnBack)
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

@Preview(showBackground = true)
@Composable
private fun CategoryFilterRowPreview() {
    MaterialTheme {
        CategoryFilterRow(
            categories = listOf("Food", "Coffee", "Transport"),
            selectedCategory = "Food",
            onSelect = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsUiRoutePreview() {
    MaterialTheme {
        TransactionsUiRoute(
            uiState = TransactionsUiState.Success(
                transactions = listOf(
                    Transaction("1", 42.0, "Food", "Lunch", 1000L)
                ),
                categories = listOf("Food")
            ),
            billsUiState = com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState.Success(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsListPanePreview() {
    MaterialTheme {
        TransactionsListPane(
            uiState = TransactionsUiState.Success(
                transactions = listOf(
                    Transaction("1", 42.0, "Food", "Lunch", 1000L),
                    Transaction("2", 15.0, "Coffee", "Latte", 2000L)
                ),
                categories = listOf("Food", "Coffee")
            ),
            billsUiState = com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState.Success(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionDetailPanePreview() {
    MaterialTheme {
        TransactionDetailPane(
            uiState = TransactionsUiState.Success(
                selectedTransaction = Transaction("1", 42.0, "Food", "Lunch with friends", 1000L)
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
