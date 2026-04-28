package com.zoewave.probase.seaweed.features.spendingcontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
                Text(
                    text = "${state.merchantName} - ${CurrencyUtils.formatCents(state.amountCents)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Reason: ${state.reason}", color = MaterialTheme.colorScheme.error)
                
                state.impactMessage?.let { message ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text("How would you like to proceed?")
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
