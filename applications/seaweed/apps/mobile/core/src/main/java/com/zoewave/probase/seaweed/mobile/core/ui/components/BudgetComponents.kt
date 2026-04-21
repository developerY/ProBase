package com.zoewave.probase.seaweed.mobile.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

@Preview(showBackground = true)
@Composable
private fun CategoryBudgetProgressBarPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryBudgetProgressBar(
                category = CategoryOverview("Food", 42.0, 1, 100.0)
            )
            CategoryBudgetProgressBar(
                category = CategoryOverview("Entertainment", 120.0, 1, 100.0)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UnallocatedMoneyCardPreview() {
    MaterialTheme {
        UnallocatedMoneyCard(unallocatedAmount = 123.45, modifier = Modifier.padding(16.dp))
    }
}
