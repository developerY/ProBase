package com.zoewave.probase.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseBar(
    onAdjustAmount: (Double) -> Unit,
    modifier: Modifier = Modifier,
    amounts: List<Double> = listOf(1.0, 5.0, 10.0, 20.0, 50.0)
) {
    var selectedAmount by remember { mutableDoubleStateOf(amounts.getOrElse(2) { 10.0 }) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilledTonalIconButton(
            onClick = { onAdjustAmount(-selectedAmount) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.core_ui_quick_expense_subtract))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            amounts.forEach { amount ->
                val isSelected = selectedAmount == amount
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedAmount = amount },
                    label = { Text(stringResource(R.string.core_ui_quick_expense_amount_format, amount.toInt())) }
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        FilledTonalIconButton(
            onClick = { onAdjustAmount(selectedAmount) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.core_ui_quick_expense_add))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickExpenseBarPreview() {
    MaterialTheme {
        QuickExpenseBar(onAdjustAmount = {})
    }
}
