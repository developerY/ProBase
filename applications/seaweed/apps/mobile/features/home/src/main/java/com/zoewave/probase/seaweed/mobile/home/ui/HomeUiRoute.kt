package com.zoewave.probase.seaweed.mobile.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.mobile.core.ui.components.CategoryBudgetProgressBar
import com.zoewave.probase.seaweed.mobile.core.ui.components.DonutChart
import com.zoewave.probase.seaweed.mobile.core.ui.components.FixedCostsSummaryCard
import com.zoewave.probase.seaweed.mobile.core.ui.components.RealMoneyHeroCard
import com.zoewave.probase.seaweed.mobile.core.ui.components.UnallocatedMoneyCard
import com.zoewave.probase.seaweed.mobile.home.R
import com.zoewave.probase.seaweed.mobile.transaction.ui.components.TransactionItem
import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.FinancialProfile
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import kotlin.math.absoluteValue

@Composable
fun HomeUiRoute(
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier,
        topBarActions = topBarActions
    )
}

@Composable
internal fun HomeUiRoute(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    HomeScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo,
        modifier = modifier,
        topBarActions = topBarActions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == androidx.window.core.layout.WindowWidthSizeClass.EXPANDED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_summary_title)) },
                actions = {
                    topBarActions()
                    IconButton(onClick = { onEvent(HomeUiEvent.AddRandomTransaction) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_home_add_random_data)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        when (uiState) {
            HomeUiState.Loading -> {
                HomeLoadingScreen(padding)
            }
            is HomeUiState.Success -> {
                if (isExpanded) {
                    HomeExpandedScreen(uiState, onEvent, navTo, padding)
                } else {
                    HomeCompactScreen(uiState, onEvent, navTo, padding)
                }
            }
        }
    }
}

@Composable
private fun HomeLoadingScreen(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomeExpandedScreen(
    uiState: HomeUiState.Success,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    padding: PaddingValues
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val profile = uiState.profile

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })) {
                SectionHeader(title = stringResource(R.string.applications_seaweed_apps_mobile_features_home_financial_status))
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })) {
                RequiredVsOptionalChart(uiState = uiState)
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })) {
                RealMoneyHeroCard(
                    flexibleRemainingCents = profile.flexibleMoneyRemainingCents,
                    monthProgress = profile.monthProgress
                )
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 65 })) {
                CashFlowAwarenessCard(onClick = { navTo(SeaweedDestination.CashFlow) })
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 68 })) {
                SmartCameraPromotionCard(onClick = { navTo(SeaweedDestination.SmartCamera) })
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 70 })) {
                EnvelopePromotionCard(onClick = { navTo(SeaweedDestination.Envelopes) })
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 80 })) {
                FixedCostsSummaryCard(
                    totalFixedCostsCents = profile.totalFixedCostsCents,
                    incomeCents = profile.monthlyIncomeCents,
                    navTo = navTo
                )
            }

            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })) {
                SectionHeader(title = stringResource(R.string.applications_seaweed_apps_mobile_features_home_spending_breakdown), modifier = Modifier.padding(top = 8.dp))
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 120 })) {
                OverviewSummaryCard(
                    categories = profile.categoryOverviews,
                    navTo = navTo,
                    onAnalyticsClick = { navTo(SeaweedDestination.Analytics) }
                )
            }
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 140 })) {
                UnallocatedMoneyCard(unallocatedAmountCents = profile.unallocatedMoneyCents)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 160 })) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_recent_transactions),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_view_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { navTo(SeaweedDestination.Transactions(null)) }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactions.take(10), key = { it.id }) { transaction ->
                    val categoryName = profile.categoryOverviews.find { it.id == transaction.categoryId }?.name ?: transaction.categoryId
                    AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 180 })) {
                        TransactionItem(
                            transaction = transaction,
                            categoryName = categoryName,
                            onDelete = { onEvent(HomeUiEvent.DeleteTransaction(transaction.id)) },
                            onClick = { navTo(SeaweedDestination.Transactions(category = null, transactionId = transaction.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCompactScreen(
    uiState: HomeUiState.Success,
    onEvent: (HomeUiEvent) -> Unit,
    navTo: (SeaweedDestination) -> Unit,
    padding: PaddingValues
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val profile = uiState.profile

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                SectionHeader(title = stringResource(R.string.applications_seaweed_apps_mobile_features_home_financial_status))
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
            ) {
                RequiredVsOptionalChart(uiState = uiState)
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })
            ) {
                RealMoneyHeroCard(
                    flexibleRemainingCents = profile.flexibleMoneyRemainingCents,
                    monthProgress = profile.monthProgress
                )
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 65 })
            ) {
                CashFlowAwarenessCard(onClick = { navTo(SeaweedDestination.CashFlow) })
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 68 })
            ) {
                SmartCameraPromotionCard(onClick = { navTo(SeaweedDestination.SmartCamera) })
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 70 })
            ) {
                EnvelopePromotionCard(onClick = { navTo(SeaweedDestination.Envelopes) })
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 80 })
            ) {
                FixedCostsSummaryCard(
                    totalFixedCostsCents = profile.totalFixedCostsCents,
                    incomeCents = profile.monthlyIncomeCents,
                    navTo = navTo
                )
            }
        }
        
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })
            ) {
                SectionHeader(title = stringResource(R.string.applications_seaweed_apps_mobile_features_home_spending_breakdown), modifier = Modifier.padding(top = 8.dp))
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 120 })
            ) {
                OverviewSummaryCard(
                    categories = uiState.profile.categoryOverviews,
                    navTo = navTo,
                    onAnalyticsClick = { navTo(SeaweedDestination.Analytics) }
                )
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 140 })
            ) {
                UnallocatedMoneyCard(unallocatedAmountCents = profile.unallocatedMoneyCents)
            }
        }
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 160 })
            ) {
                OutlinedButton(
                    onClick = { navTo(SeaweedDestination.CategoryGrid) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_all_categories))
                }
            }
        }
        
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 180 })
            ) {
                SectionHeader(title = stringResource(R.string.applications_seaweed_apps_mobile_features_home_recent_transactions), modifier = Modifier.padding(top = 8.dp))
            }
        }
        items(uiState.transactions.take(5), key = { it.id }) { transaction ->
            val categoryName = profile.categoryOverviews.find { it.id == transaction.categoryId }?.name ?: transaction.categoryId
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 200 })
            ) {
                TransactionItem(
                    transaction = transaction,
                    categoryName = categoryName,
                    onDelete = { onEvent(HomeUiEvent.DeleteTransaction(transaction.id)) },
                    onClick = { navTo(SeaweedDestination.Transactions(category = null, transactionId = transaction.id)) }
                )
            }
        }
    }
}

@Composable
fun SmartCameraPromotionCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Can I Afford This?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Use AI Camera to check item impact",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun CashFlowAwarenessCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.applications_seaweed_apps_mobile_features_home_cash_flow_awareness),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.applications_seaweed_apps_mobile_features_home_cash_flow_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EnvelopePromotionCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.applications_seaweed_apps_mobile_features_home_decision_control),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.applications_seaweed_apps_mobile_features_home_decision_control_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun RequiredVsOptionalChart(uiState: HomeUiState.Success) {
    val transactions = uiState.transactions
    
    val requiredSpending = transactions.filter { it.effectiveType == SpendingType.NEED }.sumOf { it.amountCents }.absoluteValue
    val optionalSpending = transactions.filter { it.effectiveType == SpendingType.WANT }.sumOf { it.amountCents }.absoluteValue
    
    val totalVariable = requiredSpending + optionalSpending
    if (totalVariable == 0L) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_needs_vs_wants), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_wants_explainer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                val requiredWeight = (requiredSpending.toFloat() / totalVariable).coerceIn(0.01f, 0.99f)
                val optionalWeight = (optionalSpending.toFloat() / totalVariable).coerceIn(0.01f, 0.99f)
                
                Box(modifier = Modifier.weight(requiredWeight).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)))
                Box(modifier = Modifier.weight(optionalWeight).fillMaxHeight().background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)))
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_required), style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_currency_format, CurrencyUtils.formatCents(requiredSpending)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_optional), style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_currency_format, CurrencyUtils.formatCents(optionalSpending)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            
            if (optionalSpending > 0) {
                Spacer(Modifier.height(12.dp))
                val annualSavings = optionalSpending * 0.2 * 12
                Text(
                    text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_savings_projection, CurrencyUtils.formatCents(annualSavings.toLong())),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AnalyticsPromotionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(64.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = CircleShape,
        shadowElevation = 6.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_features_home_analytics),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_analytics),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun OverviewSummaryCard(
    categories: List<CategoryOverview>,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier,
    onAnalyticsClick: (() -> Unit)? = null
) {
    val totalSpendingCents = categories.sumOf { it.totalAmountCents }
    val totalTransactions = categories.sumOf { it.transactionCount }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_spending_summary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_currency_format, CurrencyUtils.formatCents(totalSpendingCents)),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = stringResource(R.string.applications_seaweed_apps_mobile_features_home_transactions_count, totalTransactions, categories.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChart(
                            spendingByCategory = categories.associate { it.name to it.totalAmountCents.toDouble() },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (onAnalyticsClick != null) {
                            AnalyticsPromotionCard(
                                onClick = onAnalyticsClick
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categories.take(3).forEach { category ->
                        CategoryBudgetProgressBar(category = category)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                TextButton(
                    onClick = { navTo(SeaweedDestination.Budget) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_features_home_manage_budgets))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenSuccessPreview() {
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
                transactions = listOf(
                    Transaction(
                        id = "1",
                        amountCents = -4200L,
                        categoryId = "food_id",
                        description = "Lunch",
                        timestamp = System.currentTimeMillis(),
                        defaultType = SpendingType.NEED
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
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
