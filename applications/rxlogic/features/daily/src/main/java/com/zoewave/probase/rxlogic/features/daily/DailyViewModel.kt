package com.zoewave.probase.rxlogic.features.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.rxlogic.data.MedicationRepository
import com.zoewave.probase.rxlogic.model.LogStatus
import com.zoewave.probase.rxlogic.model.MedicationLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    val uiState: StateFlow<DailyUiState> = combine(
        medicationRepository.getMedications(),
        medicationRepository.getLogs()
    ) { medications, logs ->
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date

        val tasks = medications.flatMap { med ->
            med.reminderTimes.map { time ->
                val log = logs.find { 
                    it.medicationId == med.id && 
                    it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date == today
                }
                DailyMedicationTask(
                    medicationId = med.id,
                    name = med.name,
                    dosage = med.dosage,
                    scheduledTime = time,
                    instructions = med.instructions,
                    frequency = med.frequency,
                    status = when (log?.status) {
                        LogStatus.TAKEN -> DailyTaskStatus.TAKEN
                        LogStatus.SKIPPED -> DailyTaskStatus.SKIPPED
                        LogStatus.MISSED -> DailyTaskStatus.MISSED
                        null -> if (time < now.time) DailyTaskStatus.MISSED else DailyTaskStatus.UPCOMING
                    }
                )
            }
        }.sortedBy { it.scheduledTime }

        val weeklyAdherence = (0..6).map { daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            val dayLogs = logs.filter { it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date == date }
            val dayMeds = medications.filter {
                // Simple logic: assume same schedule for all days for now
                true 
            }
            val totalNeeded = dayMeds.sumOf { it.reminderTimes.size }
            val taken = dayLogs.count { it.status == LogStatus.TAKEN }
            
            AdherenceDay(
                date = date,
                isFullyTaken = totalNeeded > 0 && taken >= totalNeeded,
                totalMeds = totalNeeded,
                takenMeds = taken
            )
        }.reversed()

        DailyUiState(
            medications = tasks,
            stats = DailyStats(
                totalMeds = tasks.size,
                takenMeds = tasks.count { it.status == DailyTaskStatus.TAKEN }
            ),
            weeklyAdherence = weeklyAdherence,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyUiState(isLoading = true)
    )

    fun onEvent(event: DailyEvent) {
        when (event) {
            is DailyEvent.OnMarkAsTaken -> markAsTaken(event.medicationId)
            DailyEvent.OnRefresh -> { /* Refresh logic if needed */ }
        }
    }

    private fun markAsTaken(medicationId: String) {
        viewModelScope.launch {
            val log = MedicationLog(
                id = UUID.randomUUID().toString(),
                medicationId = medicationId,
                timestamp = Clock.System.now(),
                status = LogStatus.TAKEN
            )
            medicationRepository.insertLog(log)
        }
    }
}
