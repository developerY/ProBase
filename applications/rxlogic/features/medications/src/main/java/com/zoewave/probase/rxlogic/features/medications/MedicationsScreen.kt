package com.zoewave.probase.rxlogic.features.medications

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.Medication
import com.zoewave.probase.rxlogic.model.navigation.RxLogicRoute
import kotlinx.datetime.LocalTime

@Composable
fun MedicationsScreen(
    uiState: MedicationsUiState,
    onEvent: (MedicationsEvent) -> Unit,
    navTo: (RxLogicRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Text(
                text = "Medications",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(MedicationsEvent.OnShowAddDialog(true)) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else {
            MedicationList(
                uiState = uiState.medications,
                onEvent = onEvent,
                navTo = navTo,
                modifier = Modifier.padding(innerPadding)
            )
        }

        if (uiState.showAddDialog) {
            AddMedicationDialog(
                uiState = Unit,
                onEvent = onEvent,
                navTo = navTo
            )
        }
    }
}

@Composable
fun MedicationList(
    uiState: List<Medication>,
    onEvent: (MedicationsEvent) -> Unit,
    navTo: (RxLogicRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isEmpty()) {
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
            items(uiState, key = { it.id }) { medication ->
                MedicationItem(
                    uiState = medication,
                    onEvent = onEvent,
                    navTo = navTo
                )
            }
        }
    }
}

@Composable
fun MedicationItem(
    uiState: Medication,
    onEvent: (MedicationsEvent) -> Unit,
    navTo: (RxLogicRoute) -> Unit,
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
                Text(text = uiState.name, style = MaterialTheme.typography.titleLarge)
                Text(text = uiState.dosage, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = uiState.reminderTimes.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = { onEvent(MedicationsEvent.OnMarkAsTaken(uiState.id)) }) {
                Icon(Icons.Default.Check, contentDescription = "Mark as taken")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationsScreenPreview() {
    MedicationsScreen(
        uiState = MedicationsUiState(
            medications = listOf(
                Medication(
                    id = "1",
                    name = "Aspirin",
                    dosage = "100mg",
                    frequency = Frequency.DAILY,
                    reminderTimes = listOf(LocalTime(8, 0))
                )
            )
        ),
        onEvent = {},
        navTo = {}
    )
}

@Preview(showBackground = true)
@Composable
fun MedicationItemPreview() {
    MedicationItem(
        uiState = Medication(
            id = "1",
            name = "Aspirin",
            dosage = "100mg",
            frequency = Frequency.DAILY,
            reminderTimes = listOf(LocalTime(8, 0))
        ),
        onEvent = {},
        navTo = {}
    )
}
