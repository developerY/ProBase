package com.zoewave.probase.rxlogic.data

import com.zoewave.probase.rxlogic.db.MedicationDao
import com.zoewave.probase.rxlogic.db.MedicationEntity
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.Medication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationRepositoryTest {

    private val medicationDao: MedicationDao = mockk()
    private val repository = OfflineMedicationRepository(medicationDao)

    @Test
    fun getMedications_returnsMappedModels() = runTest {
        val entities = listOf(
            MedicationEntity(
                id = "1",
                name = "Test",
                dosage = "10mg",
                frequency = Frequency.DAILY,
                reminderTimes = emptyList(),
                instructions = null
            )
        )
        coEvery { medicationDao.getAllMedications() } returns flowOf(entities)

        val result = repository.getMedications().first()

        assertEquals(1, result.size)
        assertEquals("Test", result[0].name)
    }

    @Test
    fun insertMedication_callsDao() = runTest {
        val medication = Medication(
            id = "1",
            name = "Test",
            dosage = "10mg",
            frequency = Frequency.DAILY,
            reminderTimes = emptyList()
        )
        coEvery { medicationDao.insertMedication(any()) } returns Unit

        repository.insertMedication(medication)

        coVerify { medicationDao.insertMedication(any()) }
    }
}
