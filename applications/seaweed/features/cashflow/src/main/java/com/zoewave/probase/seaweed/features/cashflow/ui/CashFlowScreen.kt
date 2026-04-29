package com.zoewave.probase.seaweed.features.cashflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.features.cashflow.R
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowAwareness
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowSummary
import kotlinx.datetime.LocalDate

@Composable
fun CashFlowUiRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CashFlowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CashFlowScreen(
        uiState = uiState,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowScreen(
    uiState: CashFlowUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_seaweed_features_cashflow_title), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_seaweed_features_cashflow_back))
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    uiState.awareness?.let { awareness ->
                        AwarenessHero(awareness = awareness)
                    }
                }

                item {
                    uiState.summary?.let { summary ->
                        CashFlowMetricsCard(summary = summary)
                    }
                }

                item {
                    NetBalanceVisualizer(summary = uiState.summary)
                }

                item {
                    CashFlowPhilosophy()
                }
            }
        }
    }
}

@Composable
private fun AwarenessHero(awareness: CashFlowAwareness) {
    val (color, icon) = when (awareness) {
        is CashFlowAwareness.Positive -> MaterialTheme.colorScheme.primary to Icons.Default.TrendingUp
        is CashFlowAwareness.Warning -> MaterialTheme.colorScheme.error to Icons.Default.Warning
        is CashFlowAwareness.Neutral -> MaterialTheme.colorScheme.secondary to Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = when (awareness) {
                    is CashFlowAwareness.Positive -> awareness.message
                    is CashFlowAwareness.Warning -> awareness.message
                    is CashFlowAwareness.Neutral -> awareness.message
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun CashFlowMetricsCard(summary: CashFlowSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricRow(
                label = stringResource(R.string.applications_seaweed_features_cashflow_money_in),
                value = CurrencyUtils.formatCents(summary.incomeCents),
                color = MaterialTheme.colorScheme.primary
            )
            MetricRow(
                label = stringResource(R.string.applications_seaweed_features_cashflow_money_out),
                value = CurrencyUtils.formatCents(summary.expensesCents),
                color = MaterialTheme.colorScheme.error
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            MetricRow(
                label = stringResource(R.string.applications_seaweed_features_cashflow_net_flow),
                value = CurrencyUtils.formatCents(summary.netBalanceCents),
                color = if (summary.netBalanceCents >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                isLarge = true
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, color: Color, isLarge: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
private fun NetBalanceVisualizer(summary: CashFlowSummary?) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.applications_seaweed_features_cashflow_savings_rate), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        LinearProgressIndicator(
            progress = { summary?.savingsRate ?: 0f },
            modifier = Modifier.fillMaxWidth().height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.Transparent
        )
        Text(
            text = stringResource(R.string.applications_seaweed_features_cashflow_target_rate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CashFlowPhilosophy() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.applications_seaweed_features_cashflow_vs_envelopes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = stringResource(R.string.applications_seaweed_features_cashflow_philosophy),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CashFlowScreenPreview() {
    MaterialTheme {
        CashFlowScreen(
            uiState = CashFlowUiState(
                summary = CashFlowSummary(
                    month = LocalDate(2026, 4, 1),
                    incomeCents = 500000L,
                    expensesCents = 350000L,
                    netBalanceCents = 150000L,
                    savingsRate = 0.3f,
                    dailyPaceCents = 11000L,
                    projectedEndBalanceCents = 170000L
                ),
                awareness = CashFlowAwareness.Positive("You are saving 30% of your income.", 150000L)
            ),
            onBack = {}
        )
    }
}
