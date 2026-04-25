package com.zoewave.probase.seaweed.mobile.bills.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.bills.R
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale
import com.zoewave.probase.core.ui.R as CoreUiR

@Composable
fun BillsUiRoute(
    onBack: () -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BillsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BillsUiRoute(
        uiState = uiState,
        onEvent = { event ->
            if (event is BillsUiEvent.OnBackClicked) {
                onBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        navTo = navTo,
        modifier = modifier
    )
}

@Composable
internal fun BillsUiRoute(
    uiState: BillsUiState,
    onEvent: (BillsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    BillsScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BillsScreen(
    uiState: BillsUiState,
    onEvent: (BillsUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_bills_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BillsUiEvent.OnBackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(CoreUiR.string.cd_navigate_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Show Add Dialog */ }) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_bills_add)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState) {
                BillsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BillsUiState.Success -> {
                    val groupedExpenses = uiState.expenses.groupBy { it.categoryId }
                    
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
                        
                        groupedExpenses.forEach { (categoryId, expenses) ->
                            stickyHeader {
                                CategoryHeader(categoryId = categoryId)
                            }
                            items(expenses, key = { it.id }) { expense ->
                                BillItem(
                                    expense = expense,
                                    onAmountChange = { onEvent(BillsUiEvent.UpdateExpenseAmount(expense.id, it)) },
                                    onImportanceChange = { onEvent(BillsUiEvent.UpdateExpenseImportance(expense.id, it)) },
                                    onDelete = { onEvent(BillsUiEvent.DeleteExpense(expense.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillImpactHeader(income: Double, totalCosts: Double) {
    val remaining = income - totalCosts
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_bills_impact_header), 
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_bills_currency_format, String.format(Locale.getDefault(), "%.2f", remaining)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.applications_seaweed_apps_mobile_features_bills_after_fixed, 
                    String.format(Locale.getDefault(), "%.0f", totalCosts)
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CategoryHeader(categoryId: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = categoryId, // TODO: look up name from repository or map
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun BillItem(
    expense: RecurringExpense,
    onAmountChange: (Double) -> Unit,
    onImportanceChange: (SpendingType) -> Unit,
    onDelete: () -> Unit
) {
    var textValue by remember(expense.averageAmountCents) { 
        val dollars = expense.averageAmountCents.toDouble() / 100.0
        mutableStateOf(if (dollars == 0.0) "" else String.format(Locale.getDefault(), "%.2f", dollars)) 
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expense.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (expense.defaultType == SpendingType.NEED) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (expense.defaultType == SpendingType.NEED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = expense.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (expense.defaultType == SpendingType.NEED) "Required" else "Optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (expense.defaultType == SpendingType.NEED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.clickable { 
                        onImportanceChange(
                            if (expense.defaultType == SpendingType.NEED) SpendingType.WANT else SpendingType.NEED
                        )
                    }
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { 
                        textValue = it
                        it.toDoubleOrNull()?.let { amount -> onAmountChange(amount) }
                    },
                    modifier = Modifier.width(90.dp),
                    label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_bills_amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = stringResource(CoreUiR.string.action_delete), 
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BillImpactHeaderPreview() {
    MaterialTheme {
        BillImpactHeader(income = 5000.0, totalCosts = 1260.0)
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryHeaderPreview() {
    MaterialTheme {
        CategoryHeader(categoryId = "housing_id")
    }
}

@Preview(showBackground = true)
@Composable
private fun BillItemPreview() {
    MaterialTheme {
        BillItem(
            expense = RecurringExpense("1", "Rent", 120000L, ExpenseFrequency.MONTHLY, "housing_id"),
            onAmountChange = {},
            onImportanceChange = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BillsUiRoutePreview() {
    MaterialTheme {
        BillsUiRoute(
            uiState = BillsUiState.Success(
                expenses = listOf(
                    RecurringExpense("1", "Rent", 120000L, ExpenseFrequency.MONTHLY, "housing_id")
                ),
                monthlyIncome = 5000.0,
                totalFixedCosts = 1200.0
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BillsScreenPreview() {
    MaterialTheme {
        BillsScreen(
            uiState = BillsUiState.Success(
                expenses = listOf(
                    RecurringExpense("1", "Rent", 120000L, ExpenseFrequency.MONTHLY, "housing_id"),
                    RecurringExpense("2", "Internet", 6000L, ExpenseFrequency.MONTHLY, "utilities_id")
                ),
                monthlyIncome = 5000.0,
                totalFixedCosts = 1260.0
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BillsScreenLoadingPreview() {
    MaterialTheme {
        BillsScreen(
            uiState = BillsUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
