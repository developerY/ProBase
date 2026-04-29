package com.zoewave.probase.seaweed.wear.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.zoewave.probase.seaweed.wear.features.home.R
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.FinancialProfile
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
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
    val listState = rememberScalingLazyListState()

    ScreenScaffold(
        scrollState = listState,
        modifier = modifier
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (uiState) {
                HomeUiState.Loading -> {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 20.dp))
                    }
                }
                is HomeUiState.Success -> {
                    val profile = uiState.profile
                    
                    item {
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_wear_features_home_real_money),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        val balanceDollars = profile.flexibleMoneyRemainingCents.toDouble() / 100.0
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_wear_features_home_currency_format, String.format(Locale.getDefault(), "%.0f", balanceDollars)),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    item {
                        LinearProgressIndicator(
                            progress = { profile.monthProgress },
                            modifier = Modifier.width(100.dp).height(6.dp)
                        )
                    }

                    item {
                        ListHeader {
                            Text(stringResource(R.string.applications_seaweed_apps_wear_features_home_envelopes))
                        }
                    }

                    if (profile.categoryOverviews.isNotEmpty()) {
                        items(profile.categoryOverviews.filter { it.limitAmountCents != null }) { budget ->
                            val remainingDollars = (budget.remainingAmountCents ?: 0L).toDouble() / 100.0
                            val isOverBudget = (budget.remainingAmountCents ?: 0L) < 0
                            
                            Card(
                                onClick = { /* Navigate to category details if added later */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOverBudget) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = budget.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stringResource(R.string.applications_seaweed_apps_wear_features_home_currency_format, String.format(Locale.getDefault(), "%.0f", remainingDollars)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOverBudget) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    LinearProgressIndicator(
                                        progress = { budget.progressPercentage },
                                        modifier = Modifier.fillMaxWidth().height(4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ListHeader {
                            Text(stringResource(R.string.applications_seaweed_apps_wear_features_home_recent))
                        }
                    }

                    items(uiState.recentTransactions) { transaction ->
                        val isExpense = transaction.amountCents < 0
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
                                        text = transaction.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = transaction.categoryId,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 8.sp
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "$%.0f", Math.abs(transaction.amountCents.toDouble() / 100.0)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Button(
                            onClick = { onEvent(HomeUiEvent.NavigateToTransactions) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.applications_seaweed_apps_wear_features_home_spend))
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { onEvent(HomeUiEvent.NavigateToBills) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.applications_seaweed_apps_wear_features_home_bills))
                            }
                        }
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
                profile = FinancialProfile(
                    monthlyIncomeCents = 500000L,
                    totalFixedCostsCents = 150000L,
                    realStartingBalanceCents = 350000L,
                    monthlyVariableSpendingCents = 120000L,
                    flexibleMoneyRemainingCents = 230000L,
                    totalBudgetedAmountCents = 200000L,
                    unallocatedMoneyCents = 150000L,
                    categoryOverviews = listOf(
                        CategoryOverview("food_id", "Food", 40000L, 15, 50000L, 10000L, 0.8f),
                        CategoryOverview("coffee_id", "Coffee", 15000L, 20, 10000L, -5000L, 1.5f)
                    ),
                    monthProgress = 0.5f
                ),
                recentTransactions = listOf(
                    Transaction("1", -4200L, "food_id", "Lunch at Cafe", 1000L, defaultType = SpendingType.NEED)
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
