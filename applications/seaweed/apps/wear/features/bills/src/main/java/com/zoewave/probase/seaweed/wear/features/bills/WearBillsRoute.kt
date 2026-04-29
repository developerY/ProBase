package com.zoewave.probase.seaweed.wear.features.bills

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.zoewave.probase.seaweed.wear.features.bills.R
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale

@Composable
fun WearBillsRoute(
    modifier: Modifier = Modifier,
    viewModel: WearBillsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WearBillsScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                WearBillsUiEvent.NavigateBack -> onBack()
            }
        },
        navTo = {},
        modifier = modifier
    )
}

@Composable
fun WearBillsScreen(
    uiState: WearBillsUiState,
    onEvent: (WearBillsUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberScalingLazyListState()

    ScreenScaffold(
        scrollState = listState,
        modifier = modifier
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (uiState) {
                WearBillsUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }
                is WearBillsUiState.Success -> {
                    item {
                        ListHeader {
                            Text(
                                text = stringResource(R.string.applications_seaweed_apps_wear_features_bills_fixed_costs),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_wear_features_bills_monthly_format, String.format(Locale.getDefault(), "%.0f", uiState.totalMonthlyFixedCosts)),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(uiState.expenses) { expense ->
                        BillItem(expense = expense)
                    }
                }
            }
        }
    }
}

@Composable
private fun BillItem(expense: RecurringExpense) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expense.categoryId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$${String.format(Locale.getDefault(), "%.0f", expense.averageAmountCents.toDouble() / 100.0)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun WearBillsScreenPreview() {
    MaterialTheme {
        WearBillsScreen(
            uiState = WearBillsUiState.Success(
                expenses = listOf(
                    RecurringExpense("1", "Monthly Rent", 120000L, ExpenseFrequency.MONTHLY, "housing"),
                    RecurringExpense("2", "High-speed Internet", 6000L, ExpenseFrequency.MONTHLY, "utilities")
                ),
                totalMonthlyFixedCosts = 1260.0
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun WearBillsScreenLoadingPreview() {
    MaterialTheme {
        WearBillsScreen(
            uiState = WearBillsUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
