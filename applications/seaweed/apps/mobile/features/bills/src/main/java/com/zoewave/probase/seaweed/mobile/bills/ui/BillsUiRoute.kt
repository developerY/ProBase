package com.zoewave.probase.seaweed.mobile.bills.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale

@Composable
fun BillsUiRoute(
    modifier: Modifier = Modifier,
    viewModel: BillsViewModel = hiltViewModel(),
    navTo: (SeaweedDestination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Cyclic Bills") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Show Add Dialog */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        },
        modifier = modifier
    ) { padding ->
        BillsScreen(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            onEvent = viewModel::onEvent,
            navTo = navTo
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BillsScreen(
    modifier: Modifier = Modifier,
    uiState: BillsUiState,
    onEvent: (BillsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit
) {
    Box(modifier = modifier) {
        when (uiState) {
            BillsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is BillsUiState.Success -> {
                val groupedExpenses = uiState.expenses.groupBy { it.category }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        BillImpactHeader(
                            income = uiState.monthlyIncome,
                            totalCosts = uiState.totalFixedCosts
                        )
                    }
                    
                    groupedExpenses.forEach { (category, expenses) ->
                        stickyHeader {
                            CategoryHeader(category = category)
                        }
                        items(expenses, key = { it.id }) { expense ->
                            BillItem(
                                expense = expense,
                                onAmountChange = { onEvent(BillsUiEvent.UpdateExpenseAmount(expense.id, it)) },
                                onDelete = { onEvent(BillsUiEvent.DeleteExpense(expense.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillImpactHeader(income: Double, totalCosts: Double) {
    val remaining = income - totalCosts
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Real Starting Balance", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", remaining)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "After -$${String.format(Locale.getDefault(), "%.0f", totalCosts)} in fixed monthly bills",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CategoryHeader(category: ExpenseCategory) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = category.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun BillItem(
    expense: RecurringExpense,
    onAmountChange: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var textValue by remember(expense.amount) { 
        mutableStateOf(if (expense.amount == 0.0) "" else String.format(Locale.getDefault(), "%.2f", expense.amount)) 
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = expense.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            OutlinedTextField(
                value = textValue,
                onValueChange = { 
                    textValue = it
                    it.toDoubleOrNull()?.let { amount -> onAmountChange(amount) }
                },
                modifier = Modifier.width(100.dp),
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
