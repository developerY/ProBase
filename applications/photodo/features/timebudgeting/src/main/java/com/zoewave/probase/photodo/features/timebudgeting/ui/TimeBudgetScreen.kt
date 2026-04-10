package com.zoewave.probase.photodo.features.timebudgeting.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.features.timebudgeting.ui.state.TimeBudgetUiModel
import com.zoewave.probase.photodo.features.timebudgeting.ui.state.TimeBudgetUiState

@Composable
internal fun TimeBudgetScreen(
    viewModel: TimeBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Text(
                text = "Time Budgeting",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        TimeBudgetContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun TimeBudgetContent(
    uiState: TimeBudgetUiState,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        CircularProgressIndicator(modifier = modifier.fillMaxSize())
    } else if (uiState.budgets.isEmpty()) {
        Text(
            text = "No time budgets set yet.",
            modifier = modifier.padding(16.dp)
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.budgets, key = { it.categoryId }) { budget ->
                TimeBudgetItem(budget = budget)
            }
        }
    }
}

@Composable
private fun TimeBudgetItem(
    budget: TimeBudgetUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.categoryName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = budget.period,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "${budget.loggedTimeMillis / 3600000}h / ${budget.targetTimeMillis / 3600000}h",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LinearProgressIndicator(
                progress = { budget.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (budget.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
