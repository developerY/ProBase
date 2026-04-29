package com.zoewave.probase.seaweed.wear.features.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.zoewave.probase.seaweed.wear.features.transactions.R
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionListRoute(
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TransactionListScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                TransactionListUiEvent.NavigateBack -> onBack()
            }
        },
        navTo = {},
        modifier = modifier
    )
}

@Composable
fun TransactionListScreen(
    uiState: TransactionListUiState,
    onEvent: (TransactionListUiEvent) -> Unit,
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
                TransactionListUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }
                is TransactionListUiState.Success -> {
                    item {
                        ListHeader {
                            Text(
                                text = stringResource(R.string.applications_seaweed_apps_wear_features_transactions_recent),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    items(uiState.transactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val isExpense = transaction.amountCents < 0
    
    Card(
        onClick = { /* Detail not implemented */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${transaction.categoryId} • ${formatDate(transaction.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", Math.abs(transaction.amountCents.toDouble() / 100.0))}",
                style = MaterialTheme.typography.labelMedium,
                color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private fun formatDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun TransactionListScreenPreview() {
    MaterialTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Success(
                transactions = listOf(
                    Transaction("1", -4200L, "food_id", "Lunch at Cafe", 1000L, defaultType = SpendingType.NEED),
                    Transaction("2", -1500L, "coffee_id", "Latte", 2000L, defaultType = SpendingType.WANT),
                    Transaction("3", 500000L, "salary_id", "Monthly Salary", 3000L, defaultType = SpendingType.NEED)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun TransactionListScreenLoadingPreview() {
    MaterialTheme {
        TransactionListScreen(
            uiState = TransactionListUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
