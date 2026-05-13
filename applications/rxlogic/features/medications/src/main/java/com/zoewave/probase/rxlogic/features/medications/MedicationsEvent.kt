package com.zoewave.probase.rxlogic.features.medications

import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.Medication
import kotlinx.datetime.LocalTime

sealed interface MedicationsEvent {
    data class OnAddMedication(
        val name: String,
        val dosage: String,
        val frequency: Frequency,
        val time: LocalTime
    ) : MedicationsEvent

    data class OnUpdateMedication(val medication: Medication) : MedicationsEvent

    data class OnDeleteMedication(val medication: Medication) : MedicationsEvent

    data class OnMarkAsTaken(val medicationId: String) : MedicationsEvent
    
    data class OnShowAddDialog(val show: Boolean) : MedicationsEvent
}
