package com.zoewave.probase.rxlogic.data

import com.zoewave.probase.rxlogic.db.MedicationEntity
import com.zoewave.probase.rxlogic.db.MedicationLogEntity
import com.zoewave.probase.rxlogic.model.Medication
import com.zoewave.probase.rxlogic.model.MedicationLog

fun MedicationEntity.asExternalModel() = Medication(
    id = id,
    name = name,
    dosage = dosage,
    frequency = frequency,
    reminderTimes = reminderTimes,
    instructions = instructions
)

fun Medication.asEntity() = MedicationEntity(
    id = id,
    name = name,
    dosage = dosage,
    frequency = frequency,
    reminderTimes = reminderTimes,
    instructions = instructions
)

fun MedicationLogEntity.asExternalModel() = MedicationLog(
    id = id,
    medicationId = medicationId,
    timestamp = timestamp,
    status = status
)

fun MedicationLog.asEntity() = MedicationLogEntity(
    id = id,
    medicationId = medicationId,
    timestamp = timestamp,
    status = status
)
