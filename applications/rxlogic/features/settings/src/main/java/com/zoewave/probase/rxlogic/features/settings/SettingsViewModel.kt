package com.zoewave.probase.rxlogic.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.rxlogic.data.MedicationRepository
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.Medication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnGenerateSampleData -> generateSampleData()
            SettingsEvent.OnResetData -> { /* Implement if needed */ }
        }
    }

    private fun generateSampleData() {
        viewModelScope.launch {
            for (i in 1..25) {
                val medication = Medication(
                    id = UUID.randomUUID().toString(),
                    name = "Medication $i",
                    dosage = "${i * 10}mg",
                    frequency = Frequency.DAILY,
                    reminderTimes = listOf(LocalTime(8, 0)),
                    instructions = "Take with water"
                )
                medicationRepository.insertMedication(medication)
            }
        }
    }
}
