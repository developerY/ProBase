package com.zoewave.probase.seaweed.mobile.bills.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.seaweed.mobile.bills.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    uiState: BillsUiState,
    onEvent: (BillsUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false
) {
    if (isEmbedded) {
        BillsContent(uiState, onEvent, PaddingValues(0.dp), modifier)
    } else {
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
            BillsContent(uiState, onEvent, padding)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BillsContent(
    uiState: BillsUiState,
    onEvent: (BillsUiEvent) -> Unit,
    padding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(padding)) {
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
                            val categoryName = uiState.categoryMap[categoryId] ?: categoryId
                            CategoryHeader(categoryName = categoryName)
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
private fun CategoryHeader(categoryName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = categoryName,
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

    val isRequired = expense.defaultType == SpendingType.NEED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (isRequired) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (expense.categoryId) {
                                    "housing_id" -> Icons.Default.Home
                                    "utilities_id" -> Icons.Default.ElectricBolt
                                    "comm_id" -> if (expense.name.contains("Internet", true)) Icons.Default.Language else Icons.Default.PhoneAndroid
                                    "entertainment_id" -> Icons.Default.Subscriptions
                                    else -> Icons.Default.Star
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = expense.name, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = expense.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = stringResource(CoreUiR.string.action_delete), 
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = { onImportanceChange(if (isRequired) SpendingType.WANT else SpendingType.NEED) },
                    shape = RoundedCornerShape(8.dp),
                    color = (if (isRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, (if (isRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isRequired) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = if (isRequired) stringResource(R.string.applications_seaweed_apps_mobile_features_bills_required) else stringResource(R.string.applications_seaweed_apps_mobile_features_bills_optional),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { 
                        textValue = it
                        it.toDoubleOrNull()?.let { amount -> onAmountChange(amount) }
                    },
                    modifier = Modifier.width(120.dp),
                    label = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_bills_amount_label)) },
                    prefix = { Text("$", style = MaterialTheme.typography.bodyMedium) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BillsScreenSuccessPreview() {
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
