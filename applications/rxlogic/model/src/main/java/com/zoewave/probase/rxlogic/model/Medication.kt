package com.zoewave.probase.rxlogic.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalTime

@Serializable
data class Medication(
    val id: String,
    val name: String,
    val dosage: String,
    val frequency: Frequency,
    val reminderTimes: List<LocalTime> = emptyList(),
    val instructions: String? = null
)
