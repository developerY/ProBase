package com.zoewave.probase.seaweed.wear.features.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.zoewave.probase.seaweed.model.Transaction
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
        modifier = modifier,
        uiState = uiState
    )
}

@Composable
fun TransactionListScreen(
    modifier: Modifier = Modifier,
    uiState: TransactionListUiState
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
                            Text("Recent")
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
fun TransactionItem(transaction: Transaction) {
    TitleCard(
        onClick = { /* Detail not implemented */ },
        title = { Text(transaction.description) },
        subtitle = { Text(transaction.category) },
        time = { Text(formatDate(transaction.date)) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = if (transaction.amount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
    }
}

private fun formatDate(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}
