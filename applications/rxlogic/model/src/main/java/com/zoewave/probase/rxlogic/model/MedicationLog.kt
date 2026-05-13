package com.zoewave.probase.rxlogic.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class MedicationLog(
    val id: String,
    val medicationId: String,
    val timestamp: Instant,
    val status: LogStatus
)

@Serializable
enum class LogStatus {
    TAKEN,
    SKIPPED,
    MISSED
}
