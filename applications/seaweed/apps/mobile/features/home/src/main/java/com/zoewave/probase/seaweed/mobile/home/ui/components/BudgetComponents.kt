package com.zoewave.probase.seaweed.mobile.home.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.seaweed.model.CategoryOverview
import java.util.Locale

@Composable
fun CategoryBudgetProgressBar(
    category: CategoryOverview,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            val budgetText = if (category.limitAmount != null) {
                "$${String.format(Locale.getDefault(), "%.0f", category.totalAmount)} / $${String.format(Locale.getDefault(), "%.0f", category.limitAmount)}"
            } else {
                "$${String.format(Locale.getDefault(), "%.0f", category.totalAmount)}"
            }
            Text(
                text = budgetText,
                style = MaterialTheme.typography.labelSmall,
                color = if (category.limitAmount != null && category.totalAmount > category.limitAmount!!) 
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (category.limitAmount != null) {
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { category.progressPercentage.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (category.progressPercentage > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun UnallocatedMoneyCard(
    unallocatedAmount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Unallocated Money", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", unallocatedAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Buffer",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
