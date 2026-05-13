package com.zoewave.probase.rxlogic.features.medications

import com.zoewave.probase.rxlogic.model.Medication
import com.zoewave.probase.rxlogic.model.MedicationLog

data class MedicationsUiState(
    val medications: List<Medication> = emptyList(),
    val logs: List<MedicationLog> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false
)
