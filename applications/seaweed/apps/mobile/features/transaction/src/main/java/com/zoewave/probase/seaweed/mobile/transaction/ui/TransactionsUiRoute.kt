package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.mobile.bills.ui.BillsScreen
import com.zoewave.probase.seaweed.mobile.bills.ui.BillsViewModel
import com.zoewave.probase.seaweed.mobile.transaction.R
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.model.navigation.TransactionTab
import kotlinx.coroutines.launch
import java.util.Locale
import com.zoewave.probase.core.ui.R as CoreUiR
import com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiEvent

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

    LaunchedEffect(initialCategory, initialTransactionId, initialTab) {
        viewModel.setInitialCategory(initialCategory)
        viewModel.setInitialTab(initialTab)
    }

    TransactionsScreen(
        uiState = uiState,
        billsUiState = billsUiState,
        onEvent = viewModel::onEvent,
        billsOnEvent = billsViewModel::onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    uiState: TransactionsUiState,
    billsUiState: com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState,
    onEvent: (TransactionsUiEvent) -> Unit,
    billsOnEvent: (BillsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

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
                        is TransactionsUiEvent.SelectTransaction -> {
                            onEvent(event)
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, event.id)
                            }
                        }
                        else -> onEvent(event)
                    }
                },
                billsOnEvent = billsOnEvent,
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
                            } else {
                                onEvent(event)
                            }
                        }
                        else -> onEvent(event)
                    }
                }
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionsListPane(
    uiState: TransactionsUiState,
    billsUiState: com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState,
    onEvent: (TransactionsUiEvent) -> Unit,
    billsOnEvent: (BillsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit
) {
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onConfirm = { name, amount ->
                billsOnEvent(
                    BillsUiEvent.AddExpense(
                        name = name,
                        amount = amount,
                        frequency = com.zoewave.probase.seaweed.model.ExpenseFrequency.MONTHLY,
                        category = com.zoewave.probase.seaweed.model.ExpenseCategory.OTHER
                    )
                )
                showAddBillDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_transactions_title)) },
                actions = {
                    IconButton(onClick = { navTo(SeaweedDestination.Analytics) }) {
                        Icon(
                            Icons.Default.Analytics, 
                            contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_analytics_title)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is TransactionsUiState.Success) {
                TransactionsFabMenu(
                    isExpanded = isFabMenuExpanded,
                    onToggle = { isFabMenuExpanded = !isFabMenuExpanded },
                    onAddBill = { showAddBillDialog = true; isFabMenuExpanded = false },
                    onAddTransaction = { navTo(SeaweedDestination.AddTransaction); isFabMenuExpanded = false }
                )
            }
        }
    ) { padding ->
        TransactionsContent(
            uiState = uiState,
            billsUiState = billsUiState,
            onEvent = onEvent,
            billsOnEvent = billsOnEvent,
            navTo = navTo,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun TransactionsContent(
    uiState: TransactionsUiState,
    billsUiState: com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState,
    onEvent: (TransactionsUiEvent) -> Unit,
    billsOnEvent: (BillsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        TransactionsUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is TransactionsUiState.Success -> {
            Column(modifier = modifier.fillMaxSize()) {
                PrimaryTabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = uiState.selectedTab == TransactionTab.RECENT,
                        onClick = { onEvent(TransactionsUiEvent.SelectTab(TransactionTab.RECENT)) },
                        text = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_recent_tab)) }
                    )
                    Tab(
                        selected = uiState.selectedTab == TransactionTab.CYCLIC,
                        onClick = { onEvent(TransactionsUiEvent.SelectTab(TransactionTab.CYCLIC)) },
                        text = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_cyclic_tab)) }
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
                            onEvent = billsOnEvent,
                            navTo = navTo,
                            modifier = Modifier.fillMaxSize(),
                            isEmbedded = true
                        )
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
                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_no_transactions))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredTransactions, key = { it.id }) { transaction ->
                    val categoryName = uiState.categoryMap[transaction.categoryId] ?: transaction.categoryId
                    TransactionItem(
                        transaction = transaction,
                        categoryName = categoryName,
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
private fun TransactionDetailPane(
    uiState: TransactionsUiState,
    onEvent: (TransactionsUiEvent) -> Unit
) {
    val transaction = (uiState as? TransactionsUiState.Success)?.selectedTransaction

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_details_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(TransactionsUiEvent.OnBack) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(CoreUiR.string.cd_navigate_back)
                        )
                    }
                },
                actions = {
                    if (transaction != null) {
                        IconButton(onClick = { 
                            onEvent(TransactionsUiEvent.DeleteTransaction(transaction.id))
                            onEvent(TransactionsUiEvent.OnBack)
                        }) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = stringResource(CoreUiR.string.action_delete)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (transaction != null) {
            TransactionDetailContent(
                transaction = transaction,
                categoryName = (uiState as? TransactionsUiState.Success)?.categoryMap?.get(transaction.categoryId) ?: transaction.categoryId,
                modifier = Modifier.padding(padding)
            )
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_select_to_see_details))
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    categoryName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
                Text(
                    text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_amount_label), 
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_currency_format, CurrencyUtils.formatCents(transaction.amountCents)),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (transaction.amountCents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_category), 
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(categoryName, style = MaterialTheme.typography.titleMedium)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_date), 
                        style = MaterialTheme.typography.labelSmall
                    )
                    val dateString = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.timestamp)
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionsFabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddBill: () -> Unit,
    onAddTransaction: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        onClick = onAddBill,
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("Add Bill", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = onAddBill,
                        modifier = Modifier.size(40.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Add Bill", modifier = Modifier.size(20.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        onClick = onAddTransaction,
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("Add Transaction", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = onAddTransaction,
                        modifier = Modifier.size(40.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        FloatingActionButton(onClick = onToggle) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Menu",
                modifier = Modifier.rotate(if (isExpanded) 45f else 0f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
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
                label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_all_filter)) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Bill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amountValue > 0) {
                        onConfirm(name, amountValue)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenSuccessPreview() {
    MaterialTheme {
        TransactionsScreen(
            uiState = TransactionsUiState.Success(
                transactions = listOf(
                    Transaction("1", -4200L, "food_id", "Lunch", 1000L, defaultType = SpendingType.NEED)
                ),
                filteredTransactions = listOf(
                    Transaction("1", -4200L, "food_id", "Lunch", 1000L, defaultType = SpendingType.NEED)
                ),
                categories = listOf("Food"),
                categoryMap = mapOf("food_id" to "Food")
            ),
            billsUiState = com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState.Success(
                expenses = listOf(
                    com.zoewave.probase.seaweed.model.RecurringExpense(
                        id = "1",
                        name = "Rent",
                        averageAmountCents = 120000L,
                        frequency = com.zoewave.probase.seaweed.model.ExpenseFrequency.MONTHLY,
                        categoryId = "housing_id"
                    )
                )
            ),
            onEvent = {},
            billsOnEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsScreenLoadingPreview() {
    MaterialTheme {
        TransactionsScreen(
            uiState = TransactionsUiState.Loading,
            billsUiState = com.zoewave.probase.seaweed.mobile.bills.ui.BillsUiState.Loading,
            onEvent = {},
            billsOnEvent = {},
            navTo = {}
        )
    }
}
