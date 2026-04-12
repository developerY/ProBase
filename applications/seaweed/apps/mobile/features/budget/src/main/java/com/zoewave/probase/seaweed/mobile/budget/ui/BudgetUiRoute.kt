package com.zoewave.probase.seaweed.mobile.budget.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination

@Composable
fun BudgetUiRoute(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BudgetScreen(
        uiState = uiState,
        onEvent = { event ->
            if (event is BudgetUiEvent.OnBackClicked) {
                onBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        navTo = {},
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Management") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BudgetUiEvent.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        when (uiState) {
            BudgetUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BudgetUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.categories) { category ->
                        BudgetItem(
                            category = category,
                            onUpdateLimit = { newLimit ->
                                onEvent(BudgetUiEvent.UpdateBudget(category.name, newLimit))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetItem(
    category: CategoryOverview,
    onUpdateLimit: (Double) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var limitInput by remember { mutableStateOf(category.limitAmount.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDialog = true }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(category.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 
                    val limit = category.limitAmount ?: 0.0
                    (category.totalAmount / limit.coerceAtLeast(1.0)).toFloat() 
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spent: $${category.totalAmount}", style = MaterialTheme.typography.bodySmall)
                Text("Limit: $${category.limitAmount ?: "No limit"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Update Limit for ${category.name}") },
            text = {
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("New Limit") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        limitInput.toDoubleOrNull()?.let { onUpdateLimit(it) }
                        showDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetScreenPreview() {
    MaterialTheme {
        BudgetScreen(
            uiState = BudgetUiState.Success(
                categories = listOf(
                    CategoryOverview("Food", 42.0, 1, 100.0),
                    CategoryOverview("Coffee", 15.0, 1, 50.0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetScreenLoadingPreview() {
    MaterialTheme {
        BudgetScreen(
            uiState = BudgetUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
