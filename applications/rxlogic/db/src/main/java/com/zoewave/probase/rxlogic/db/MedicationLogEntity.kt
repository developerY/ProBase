package com.zoewave.probase.rxlogic.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.rxlogic.model.LogStatus
import kotlinx.datetime.Instant

@Entity(tableName = "medication_logs")
data class MedicationLogEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val timestamp: Instant,
    val status: LogStatus
)
