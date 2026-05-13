package com.zoewave.probase.rxlogic.data

import com.zoewave.probase.rxlogic.db.MedicationDao
import com.zoewave.probase.rxlogic.model.Medication
import com.zoewave.probase.rxlogic.model.MedicationLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineMedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao
) : MedicationRepository {
    override fun getMedications(): Flow<List<Medication>> =
        medicationDao.getAllMedications().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun getMedication(id: String): Medication? =
        medicationDao.getMedicationById(id)?.asExternalModel()

    override suspend fun insertMedication(medication: Medication) {
        medicationDao.insertMedication(medication.asEntity())
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication.asEntity())
    }

    override fun getLogs(): Flow<List<MedicationLog>> =
        medicationDao.getAllLogs().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun insertLog(log: MedicationLog) {
        medicationDao.insertLog(log.asEntity())
    }
}
