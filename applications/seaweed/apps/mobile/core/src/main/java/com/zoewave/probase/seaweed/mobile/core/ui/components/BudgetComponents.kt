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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.mobile.core.R
import com.zoewave.probase.seaweed.model.CategoryOverview

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
            val spentText = CurrencyUtils.formatCents(category.totalAmountCents)
            val limitText = category.limitAmountCents?.let { CurrencyUtils.formatCents(it) }
            val budgetText = if (limitText != null) {
                "$spentText / $limitText"
            } else {
                spentText
            }
            val limit = category.limitAmountCents
            Text(
                text = budgetText,
                style = MaterialTheme.typography.labelSmall,
                color = if (limit != null && category.totalAmountCents > limit)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (category.limitAmountCents != null) {
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
    unallocatedAmountCents: Long,
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
                Text(stringResource(R.string.applications_seaweed_apps_mobile_core_unallocated_money), style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "$${CurrencyUtils.formatCents(unallocatedAmountCents)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                stringResource(R.string.applications_seaweed_apps_mobile_core_buffer),
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
                category = CategoryOverview("food_id", "Food", 4200L, 1, 10000L, 5800L, 0.42f)
            )
            CategoryBudgetProgressBar(
                category = CategoryOverview("entertainment_id", "Entertainment", 12000L, 1, 10000L, -2000L, 1.2f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UnallocatedMoneyCardPreview() {
    MaterialTheme {
        UnallocatedMoneyCard(unallocatedAmountCents = 12345L, modifier = Modifier.padding(16.dp))
    }
}
