package com.zoewave.probase.rxlogic.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.rxlogic.model.Frequency
import kotlinx.datetime.LocalTime

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dosage: String,
    val frequency: Frequency,
    val reminderTimes: List<LocalTime>,
    val instructions: String?
)
