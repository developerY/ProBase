package com.zoewave.probase.features.health.data.repository

import com.zoewave.probase.features.health.data.remote.RxNavApiService
import com.zoewave.probase.features.health.data.remote.model.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalIngredientRepositoryTest {

    private val apiService = mockk<RxNavApiService>()
    private val repository = ClinicalIngredientRepositoryImpl(apiService)

    @Test
    fun `getStandardConceptId returns first rxcui when present`() = runTest {
        coEvery { apiService.getRxCui(any()) } returns RxCuiResponse(
            idGroup = IdGroup(rxnormId = listOf("123", "456"))
        )

        val result = repository.getStandardConceptId("Salicylic Acid")

        assertTrue(result.isSuccess)
        assertEquals("123", result.getOrNull())
    }

    @Test
    fun `getStandardConceptId returns null when rxnormId is empty`() = runTest {
        coEvery { apiService.getRxCui(any()) } returns RxCuiResponse(
            idGroup = IdGroup(rxnormId = emptyList())
        )

        val result = repository.getStandardConceptId("Unknown")

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun `getStandardConceptId returns failure on network error`() = runTest {
        coEvery { apiService.getRxCui(any()) } throws Exception("Network error")

        val result = repository.getStandardConceptId("Salicylic Acid")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getIngredientInteractions returns flattened descriptions`() = runTest {
        coEvery { apiService.getInteractions(any()) } returns InteractionResponse(
            interactionTypeGroup = listOf(
                InteractionTypeGroup(
                    interactionType = listOf(
                        InteractionType(
                            interactionPair = listOf(
                                InteractionPair(description = "Interaction 1"),
                                InteractionPair(description = "Interaction 2")
                            )
                        )
                    )
                )
            )
        )

        val result = repository.getIngredientInteractions("123")

        assertTrue(result.isSuccess)
        val interactions = result.getOrNull()
        assertEquals(2, interactions?.size)
        assertEquals("Interaction 1", interactions?.get(0))
        assertEquals("Interaction 2", interactions?.get(1))
    }
}
