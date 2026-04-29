package com.zoewave.probase.seaweed.wear.features.transactions

import androidx.compose.foundation.layout.*
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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            TransactionListUiState.Loading -> {
                CircularProgressIndicator()
            }
            is TransactionListUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        ListHeader {
                            Text(stringResource(R.string.applications_seaweed_apps_wear_features_transactions_recent))
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
    TitleCard(
        onClick = { /* Detail not implemented */ },
        title = { Text(transaction.description) },
        subtitle = { Text(transaction.categoryId) },
        time = { Text(formatDate(transaction.timestamp)) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", transaction.amountCents.toDouble() / 100.0)}",
            style = MaterialTheme.typography.titleLarge,
            color = if (transaction.amountCents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
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
                    Transaction("1", 4200L, "food_id", "Lunch", 1000L, defaultType = SpendingType.NEED),
                    Transaction("2", -1500L, "coffee_id", "Latte", 2000L, defaultType = SpendingType.WANT)
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
