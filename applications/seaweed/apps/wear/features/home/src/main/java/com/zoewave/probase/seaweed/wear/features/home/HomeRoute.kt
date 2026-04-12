package com.zoewave.probase.seaweed.wear.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.*
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
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
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                HomeUiEvent.NavigateToTransactions -> onTransactionsClick()
                HomeUiEvent.NavigateToBills -> onBillsClick()
                else -> viewModel.onEvent(event)
            }
        },
        navTo = {},
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    @Suppress("UnusedParameter") navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
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
                            onClick = { onEvent(HomeUiEvent.NavigateToTransactions) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Spend")
                        }
                        Button(
                            onClick = { onEvent(HomeUiEvent.NavigateToBills) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Text("Bills")
                        }
                    }
                    
                    Button(
                        onClick = { onEvent(HomeUiEvent.AddRandomTransaction) },
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

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Success(
                totalBalance = 1234.0,
                topBudgets = listOf(
                    CategoryOverview("Food", 42.0, 1, 100.0),
                    CategoryOverview("Coffee", 15.0, 1, 50.0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(device = "id:wearos_small_round", showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState.Loading,
            onEvent = {},
            navTo = {}
        )
    }
}
