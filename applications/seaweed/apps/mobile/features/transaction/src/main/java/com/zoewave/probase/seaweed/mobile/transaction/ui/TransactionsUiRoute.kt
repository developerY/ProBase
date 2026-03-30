package com.zoewave.probase.seaweed.mobile.transaction.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.features.main.navigation.SeaweedDestination
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem

@Composable
fun TransactionsUiRoute(
    modifier: Modifier = Modifier,
    initialCategory: String? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit
) {
    LaunchedEffect(initialCategory) {
        viewModel.setInitialCategory(initialCategory)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TransactionsScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    uiState: TransactionsUiState,
    onEvent: (TransactionsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transactions") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navTo(SeaweedDestination.AddTransaction) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        modifier = modifier
    ) { padding ->
        when (uiState) {
            TransactionsUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TransactionsUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CategoryFilterRow(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onSelect = { onEvent(TransactionsUiEvent.SelectCategory(it)) }
                    )
                    if (uiState.filteredTransactions.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No transactions for this category")
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
                                    onDelete = { onEvent(TransactionsUiEvent.DeleteTransaction(transaction.id)) }
                                )
                            }
                        }
                    }
                }
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
