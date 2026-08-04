package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StarterPackManagementCard(
    modifier: Modifier = Modifier,
    viewModel: StarterPackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showWipeStarterConfirm by remember { mutableStateOf(false) }

    if (showWipeStarterConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeStarterConfirm = false },
            title = { Text("Wipe Starter Pack?") },
            text = { Text("This will permanently remove all items ingested from the High-Fidelity starter pack. Your personal scans will remain untouched.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.onEvent(StarterPackEvent.OnWipeStarterPack)
                        showWipeStarterConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("WIPE DATA", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeStarterConfirm = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Developer Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Note: OnGenerateSampleData is still in SettingsViewModel for now as it uses VaultSeeder
                
                Button(
                    onClick = { viewModel.onEvent(StarterPackEvent.OnIngestStarterPack) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF745E7A)) // Luxury Purple
                ) {
                    Text("Load Starter Pack (High Fidelity)")
                }

                OutlinedButton(
                    onClick = { showWipeStarterConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text("Wipe Starter Pack", color = MaterialTheme.colorScheme.error)
                }
            }

            if (uiState.seedingState is SeedingState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            if (uiState.seedingState is SeedingState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState.seedingState as SeedingState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
