package com.zoewave.probase.seaweed.mobile.transaction.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.mobile.transaction.R
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction
import java.util.Locale
import com.zoewave.probase.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(text = transaction.categoryId, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = stringResource(R.string.applications_seaweed_apps_mobile_features_transaction_currency_format, CurrencyUtils.formatCents(transaction.amountCents)),
                style = MaterialTheme.typography.titleLarge,
                color = if (transaction.amountCents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(CoreUiR.string.action_delete))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemPreview() {
    MaterialTheme {
        TransactionItem(
            transaction = Transaction("1", 4200L, "food_id", "Lunch with friends", 1000L, defaultType = SpendingType.NEED),
            onDelete = {},
            onClick = {},
            isSelected = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemSelectedPreview() {
    MaterialTheme {
        TransactionItem(
            transaction = Transaction("1", -1500L, "coffee_id", "Morning Latte", 1000L, defaultType = SpendingType.WANT),
            onDelete = {},
            onClick = {},
            isSelected = true
        )
    }
}
