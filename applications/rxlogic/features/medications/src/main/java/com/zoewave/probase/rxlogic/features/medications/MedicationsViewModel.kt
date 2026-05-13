package com.zoewave.probase.rxlogic.features.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zoewave.probase.rxlogic.data.MedicationRepository
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.LogStatus
import com.zoewave.probase.rxlogic.model.Medication
import com.zoewave.probase.rxlogic.model.MedicationLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _showAddDialog = MutableStateFlow(false)

    val uiState: StateFlow<MedicationsUiState> = combine(
        medicationRepository.getMedications(),
        medicationRepository.getLogs(),
        _showAddDialog
    ) { medications, logs, showAddDialog ->
        MedicationsUiState(
            medications = medications,
            logs = logs,
            isLoading = false,
            showAddDialog = showAddDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MedicationsUiState(isLoading = true)
    )

    fun onEvent(event: MedicationsEvent) {
        when (event) {
            is MedicationsEvent.OnAddMedication -> {
                addMedication(event.name, event.dosage, event.frequency, event.time)
            }
            is MedicationsEvent.OnMarkAsTaken -> {
                markAsTaken(event.medicationId)
            }
            is MedicationsEvent.OnShowAddDialog -> {
                _showAddDialog.update { event.show }
            }
        }
    }

    private fun addMedication(name: String, dosage: String, frequency: Frequency, time: LocalTime) {
        viewModelScope.launch {
            val medication = Medication(
                id = UUID.randomUUID().toString(),
                name = name,
                dosage = dosage,
                frequency = frequency,
                reminderTimes = listOf(time)
            )
            medicationRepository.insertMedication(medication)
            scheduleReminder(medication)
        }
    }

    private fun scheduleReminder(medication: Medication) {
        val data = Data.Builder()
            .putString("medication_name", medication.name)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        workManager.enqueue(workRequest)
    }

    fun markAsTaken(medicationId: String) {
        viewModelScope.launch {
            val log = MedicationLog(
                id = UUID.randomUUID().toString(),
                medicationId = medicationId,
                timestamp = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                status = LogStatus.TAKEN
            )
            medicationRepository.insertLog(log)
        }
    }
}
