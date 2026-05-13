package com.zoewave.probase.rxlogic.data

import com.zoewave.probase.rxlogic.model.Medication
import com.zoewave.probase.rxlogic.model.MedicationLog
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getMedications(): Flow<List<Medication>>
    suspend fun getMedication(id: String): Medication?
    suspend fun insertMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    
    fun getLogs(): Flow<List<MedicationLog>>
    suspend fun insertLog(log: MedicationLog)
}
