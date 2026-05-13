package com.zoewave.probase.rxlogic.features.reminders

import com.zoewave.probase.rxlogic.model.Medication
import com.zoewave.probase.rxlogic.model.MedicationLog

data class RemindersUiState(
    val medications: List<Medication> = emptyList(),
    val logs: List<MedicationLog> = emptyList(),
    val isLoading: Boolean = false
)
