package com.zoewave.probase.rxlogic.features.medications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.navigation.RxLogicRoute
import kotlinx.datetime.LocalTime

@Composable
fun AddMedicationDialog(
    uiState: Unit,
    onEvent: (MedicationsEvent) -> Unit,
    navTo: (RxLogicRoute) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onEvent(MedicationsEvent.OnShowAddDialog(false)) },
        title = { Text("Add Medication") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onEvent(MedicationsEvent.OnAddMedication(name, dosage, Frequency.DAILY, LocalTime(8, 0)))
                onEvent(MedicationsEvent.OnShowAddDialog(false))
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = { onEvent(MedicationsEvent.OnShowAddDialog(false)) }) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddMedicationDialogPreview() {
    AddMedicationDialog(
        uiState = Unit,
        onEvent = {},
        navTo = {}
    )
}
