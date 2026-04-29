package com.zoewave.probase.seaweed.wear.features.bills

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            WearBillsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is WearBillsUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        ListHeader {
                            Text(stringResource(R.string.applications_seaweed_apps_wear_features_bills_fixed_costs))
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_wear_features_bills_monthly_format, String.format(Locale.getDefault(), "%.0f", uiState.totalMonthlyFixedCosts)),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
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
    TitleCard(
        onClick = { },
        title = { Text(expense.name) },
        subtitle = { Text(expense.categoryId) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", expense.averageAmountCents.toDouble() / 100.0)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black
        )
    }
}

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun WearBillsScreenPreview() {
    MaterialTheme {
        WearBillsScreen(
            uiState = WearBillsUiState.Success(
                expenses = listOf(
                    RecurringExpense("1", "Rent", 120000L, ExpenseFrequency.MONTHLY, "housing_id"),
                    RecurringExpense("2", "Internet", 6000L, ExpenseFrequency.MONTHLY, "utilities_id")
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
