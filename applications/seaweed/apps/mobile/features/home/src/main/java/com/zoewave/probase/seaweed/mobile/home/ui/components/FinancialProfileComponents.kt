package com.zoewave.probase.seaweed.mobile.home.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.seaweed.model.navigation.SeaweedDestination
import java.util.Locale

@Composable
fun RealMoneyHeroCard(
    flexibleRemaining: Double,
    monthProgress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Flexible Money Remaining",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${String.format(Locale.getDefault(), "%.2f", flexibleRemaining)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Month Progress",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LinearProgressIndicator(
                progress = { monthProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun FixedCostsSummaryCard(
    totalFixedCosts: Double,
    income: Double,
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
                    text = "Fixed Monthly Bills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { navTo(SeaweedDestination.Bills) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Manage Bills")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Income", style = MaterialTheme.typography.labelSmall)
                    Text("$${String.format(Locale.getDefault(), "%.0f", income)}", style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Bills", style = MaterialTheme.typography.labelSmall)
                    Text("-$${String.format(Locale.getDefault(), "%.0f", totalFixedCosts)}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            
            val startingBalance = income - totalFixedCosts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Real Starting Balance", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("$${String.format(Locale.getDefault(), "%.2f", startingBalance)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RealMoneyHeroCardPreview() {
    MaterialTheme {
        RealMoneyHeroCard(flexibleRemaining = 1234.56, monthProgress = 0.65f, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun FixedCostsSummaryCardPreview() {
    MaterialTheme {
        FixedCostsSummaryCard(totalFixedCosts = 1500.0, income = 5000.0, navTo = {}, modifier = Modifier.padding(16.dp))
    }
}
