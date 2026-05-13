package com.zoewave.probase.rxlogic.features.daily

import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.LogStatus
import kotlinx.datetime.LocalTime

data class DailyUiState(
    val medications: List<DailyMedicationTask> = emptyList(),
    val isLoading: Boolean = false,
    val stats: DailyStats = DailyStats(),
    val weeklyAdherence: List<AdherenceDay> = emptyList()
)

data class AdherenceDay(
    val date: kotlinx.datetime.LocalDate,
    val isFullyTaken: Boolean,
    val totalMeds: Int,
    val takenMeds: Int
)

data class DailyMedicationTask(
    val medicationId: String,
    val name: String,
    val dosage: String,
    val scheduledTime: LocalTime,
    val status: DailyTaskStatus,
    val frequency: Frequency,
    val instructions: String? = null
)

enum class DailyTaskStatus {
    UPCOMING,
    TAKEN,
    SKIPPED,
    MISSED
}

data class DailyStats(
    val totalMeds: Int = 0,
    val takenMeds: Int = 0
)

sealed interface DailyEvent {
    data class OnMarkAsTaken(val medicationId: String) : DailyEvent
    data object OnRefresh : DailyEvent
}
