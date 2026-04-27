package com.zoewave.probase.seaweed.features.spendingcontrol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.InterventionAction
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.InterventionState

@Composable
fun InterventionDialog(
    state: InterventionState,
    onAction: (InterventionAction) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaction Declined") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${state.merchantName} - ${CurrencyUtils.formatCents(state.amountCents)}")
                Text("Reason: ${state.reason}", color = MaterialTheme.colorScheme.error)
                Text("Your Dining budget is exceeded. How would you like to proceed?")
            }
        },
        confirmButton = {
            Button(onClick = { onAction(InterventionAction.Override) }) {
                Text("Approve Anyway")
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(InterventionAction.Cancel) }) {
                Text("Cancel")
            }
        }
    )
}
