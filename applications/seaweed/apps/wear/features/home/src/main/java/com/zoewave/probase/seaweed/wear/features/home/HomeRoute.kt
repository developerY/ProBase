package com.zoewave.probase.seaweed.wear.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.*
import java.util.Locale

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onTransactionsClick: () -> Unit,
    onBillsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onTransactionsClick = onTransactionsClick,
        onBillsClick = onBillsClick,
        onAddClick = { viewModel.addRandomTransaction() }
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onTransactionsClick: () -> Unit,
    onBillsClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            HomeUiState.Loading -> {
                CircularProgressIndicator()
            }
            is HomeUiState.Success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Real Money",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.0f", uiState.totalBalance)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                    
                    if (uiState.topBudgets.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            uiState.topBudgets.forEach { budget ->
                                Text(
                                    text = "${budget.name}: $${String.format(Locale.getDefault(), "%.0f", budget.remainingAmount ?: 0.0)} left",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if ((budget.remainingAmount ?: 0.0) < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = onTransactionsClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Spend")
                        }
                        Button(
                            onClick = onBillsClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Text("Bills")
                        }
                    }
                    
                    Button(
                        onClick = onAddClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Add Random")
                    }
                }
            }
        }
    }
}
