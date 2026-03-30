package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail


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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.core.ui.theme.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.features.tasks.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseBar(
    onAdjustSpend: (Double) -> Unit, // Passes +amount or -amount back to the screen
    modifier: Modifier = Modifier
) {
    // The quick-select options
    val amounts = listOf(1.0, 5.0, 10.0, 20.0, 50.0)

    // Remember which amount is currently clicked (Default to $10)
    var selectedAmount by remember { mutableStateOf(10.0) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // --- 1. MINUS BUTTON ---
        FilledTonalIconButton(
            onClick = { onAdjustSpend(-selectedAmount) }, // Pass negative amount
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_subtract_spend_content_desc))
        }

        // --- 2. SCROLLABLE NUMBER CHIPS ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.width(4.dp)) // Visual padding
            amounts.forEach { amount ->
                val isSelected = selectedAmount == amount
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedAmount = amount },
                    // Formats double (10.0) to integer string ("$10") for cleaner UI
                    label = { Text(stringResource(R.string.applications_photodo_apps_mobile_features_tasks_amount_format, amount.toInt())) }
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        // --- 3. PLUS BUTTON ---
        FilledTonalIconButton(
            onClick = { onAdjustSpend(selectedAmount) }, // Pass positive amount
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_tasks_detail_add_spend_content_desc))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuickExpenseBarPreview() {
    PhotoDoTheme {
        QuickExpenseBar(
            onAdjustSpend = {}
        )
    }
}
