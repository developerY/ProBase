package com.zoewave.probase.rxlogic.features.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.probase.rxlogic.model.LogStatus
import com.zoewave.probase.rxlogic.model.Medication

@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Text(
                text = "RxLogic Reminders",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else {
            MedicationList(
                medications = uiState.medications,
                onTakenClick = viewModel::markAsTaken,
                modifier = Modifier.padding(innerPadding)
            )
        }

        if (showAddDialog) {
            AddMedicationDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = viewModel::addMedication
            )
        }
    }
}

@Composable
fun MedicationList(
    medications: List<Medication>,
    onTakenClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (medications.isEmpty()) {
        Text(
            text = "No medications added yet.",
            modifier = modifier.padding(16.dp)
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(medications, key = { it.id }) { medication ->
                MedicationItem(
                    medication = medication,
                    onTakenClick = { onTakenClick(medication.id) }
                )
            }
        }
    }
}

@Composable
fun MedicationItem(
    medication: Medication,
    onTakenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = medication.name, style = MaterialTheme.typography.titleLarge)
                Text(text = medication.dosage, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = medication.reminderTimes.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = onTakenClick) {
                Icon(Icons.Default.Check, contentDescription = "Mark as taken")
            }
        }
    }
}
