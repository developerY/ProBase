package com.zoewave.probase.seaweed.mobile.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.mobile.core.R
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale

@Composable
fun RealMoneyHeroCard(
    flexibleRemainingCents: Long,
    monthProgress: Float,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (BoxScope.() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.applications_seaweed_apps_mobile_core_flexible_money),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${CurrencyUtils.formatCents(flexibleRemainingCents)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.applications_seaweed_apps_mobile_core_month_progress),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LinearProgressIndicator(
                    progress = { monthProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )
            }

            if (trailingContent != null) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    trailingContent()
                }
            }
        }
    }
}

@Composable
fun FixedCostsSummaryCard(
    totalFixedCostsCents: Long,
    incomeCents: Long,
    navTo: (SeaweedDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.applications_seaweed_apps_mobile_core_fixed_bills),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { navTo(SeaweedDestination.Bills) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.applications_seaweed_apps_mobile_core_manage_bills))
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_core_total_income), style = MaterialTheme.typography.labelSmall)
                    Text("$${CurrencyUtils.formatCents(incomeCents)}", style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.applications_seaweed_apps_mobile_core_total_bills), style = MaterialTheme.typography.labelSmall)
                    Text("-$${CurrencyUtils.formatCents(totalFixedCostsCents)}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            
            val startingBalanceCents = incomeCents - totalFixedCostsCents
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.applications_seaweed_apps_mobile_core_starting_balance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("$${CurrencyUtils.formatCents(startingBalanceCents)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
            }
        }
    }
}
