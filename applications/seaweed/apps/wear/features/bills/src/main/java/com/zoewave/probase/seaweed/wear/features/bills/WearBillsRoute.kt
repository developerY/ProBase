package com.zoewave.probase.seaweed.wear.features.bills

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.zoewave.probase.seaweed.model.RecurringExpense
import java.util.Locale

@Composable
fun WearBillsRoute(
    modifier: Modifier = Modifier,
    viewModel: WearBillsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WearBillsScreen(
        modifier = modifier,
        uiState = uiState
    )
}

@Composable
fun WearBillsScreen(
    modifier: Modifier = Modifier,
    uiState: WearBillsUiState
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
                            Text("Fixed Costs")
                        }
                    }
                    item {
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.0f", uiState.totalMonthlyFixedCosts)}/mo",
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
fun BillItem(expense: RecurringExpense) {
    TitleCard(
        onClick = { },
        title = { Text(expense.name) },
        subtitle = { Text(expense.category.name.lowercase()) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", expense.monthlyImpact)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black
        )
    }
}
