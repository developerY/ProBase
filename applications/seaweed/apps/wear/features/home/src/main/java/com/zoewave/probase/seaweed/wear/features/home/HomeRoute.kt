package com.zoewave.probase.seaweed.wear.features.home

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
import androidx.wear.compose.material3.*
import com.zoewave.probase.seaweed.wear.features.home.R
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
                        text = stringResource(R.string.applications_seaweed_apps_wear_features_home_real_money),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = stringResource(R.string.applications_seaweed_apps_wear_features_home_currency_format, String.format(Locale.getDefault(), "%.0f", uiState.totalBalance)),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                    
                    if (uiState.topBudgets.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            uiState.topBudgets.forEach { budget ->
                                val remainingDollars = (budget.remainingAmountCents ?: 0L).toDouble() / 100.0
                                Text(
                                    text = stringResource(R.string.applications_seaweed_apps_wear_features_home_left_format, budget.name, String.format(Locale.getDefault(), "%.0f", remainingDollars)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if ((budget.remainingAmountCents ?: 0L) < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
                            Text(stringResource(R.string.applications_seaweed_apps_wear_features_home_spend))
                        }
                        Button(
                            onClick = { onEvent(HomeUiEvent.NavigateToBills) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Text(stringResource(R.string.applications_seaweed_apps_wear_features_home_bills))
                        }
                    }
                    
                    Button(
                        onClick = { onEvent(HomeUiEvent.AddRandomTransaction) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(R.string.applications_seaweed_apps_wear_features_home_add_random))
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
                    CategoryOverview("food_id", "Food", 4200L, 1, 10000L, 5800L, 0.42f),
                    CategoryOverview("coffee_id", "Coffee", 1500L, 1, 5000L, 3500L, 0.3f)
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
