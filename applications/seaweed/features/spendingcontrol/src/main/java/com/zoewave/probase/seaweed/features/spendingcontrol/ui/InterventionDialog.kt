package com.zoewave.probase.seaweed.features.spendingcontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.features.spendingcontrol.R
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
        title = { Text(stringResource(R.string.applications_seaweed_features_spendingcontrol_declined)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${state.merchantName} - ${CurrencyUtils.formatCents(state.amountCents)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.applications_seaweed_features_spendingcontrol_reason, state.reason), 
                    color = MaterialTheme.colorScheme.error
                )
                
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

                Text(stringResource(R.string.applications_seaweed_features_spendingcontrol_how_to_proceed))
            }
        },
        confirmButton = {
            Button(onClick = { onAction(InterventionAction.Override) }) {
                Text(stringResource(R.string.applications_seaweed_features_spendingcontrol_approve_anyway))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(InterventionAction.Cancel) }) {
                Text(stringResource(R.string.applications_seaweed_features_spendingcontrol_cancel))
            }
        }
    )
}
