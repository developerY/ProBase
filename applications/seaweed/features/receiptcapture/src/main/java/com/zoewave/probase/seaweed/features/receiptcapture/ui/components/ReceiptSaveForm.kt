package com.zoewave.probase.seaweed.features.receiptcapture.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.seaweed.features.receiptcapture.domain.SmartReceiptDraft

@Composable
fun ReceiptSaveForm(
    draft: SmartReceiptDraft,
    onConfirm: (SmartReceiptDraft) -> Unit,
    onRetake: () -> Unit
) {
    var merchant by remember { mutableStateOf(draft.merchant ?: "") }
    var total by remember { mutableStateOf(draft.total?.toString() ?: "") }
    var category by remember { mutableStateOf(draft.category ?: "") }
    var date by remember { mutableStateOf(draft.date ?: "") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Review Receipt", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        label = { Text("Merchant") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = total,
                        onValueChange = { total = it },
                        label = { Text("Total Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("$") }
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onRetake, modifier = Modifier.weight(1f)) {
                    Text("Retake")
                }
                Button(
                    onClick = {
                        onConfirm(draft.copy(
                            merchant = merchant,
                            total = total.toDoubleOrNull(),
                            date = date,
                            category = category
                        ))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text("Save", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
