package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WardrobeCandidateFilterTest {

    private val repository = mockk<WardrobeRepository>()
    private lateinit var filter: WardrobeCandidateFilter

    @Before
    fun setup() {
        filter = WardrobeCandidateFilter(repository)
    }

    @Test
    fun `getCandidates should prune items based on rotation penalty`() = runTest {
        val items = listOf(
            ClothingItem(remoteId = "1", name = "Recent", category = ClothingCategory.TOPS, colorHex = "#000000"),
            ClothingItem(remoteId = "2", name = "Old", category = ClothingCategory.TOPS, colorHex = "#000000")
        )
        val context = StyleRequestContext(
            intent = "party", weather = "warm", appearanceTelemetry = "warm",
            rotationScores = mapOf("1" to 0.8, "2" to 0.1) // 1 is violated
        )

        coEvery { repository.getAllClothing() } returns flowOf(items)

        val result = filter.getCandidates(context, limit = 10)

        assertThat(result).hasSize(1)
        assertThat(result[0].remoteId).isEqualTo("2")
    }

    @Test
    fun `getCandidates should prioritize anchored items`() = runTest {
        val items = listOf(
            ClothingItem(internalId = 1, remoteId = "1", name = "Standard", category = ClothingCategory.TOPS, colorHex = "#000000"),
            ClothingItem(internalId = 2, remoteId = "2", name = "Anchored", category = ClothingCategory.TOPS, colorHex = "#000000")
        )
        val context = StyleRequestContext(
            intent = "party", weather = "warm", appearanceTelemetry = "warm",
            anchoredClothingIds = listOf("w_2")
        )

        coEvery { repository.getAllClothing() } returns flowOf(items)

        val result = filter.getCandidates(context, limit = 10)

        assertThat(result).hasSize(2)
        assertThat(result[0].internalId).isEqualTo(2L) // Anchored item first
    }

    @Test
    fun `getCandidates should respect the limit`() = runTest {
        val items = List(10) { 
            ClothingItem(internalId = it.toLong(), remoteId = "$it", name = "Item $it", category = ClothingCategory.TOPS, colorHex = "#000000")
        }
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = "warm")

        coEvery { repository.getAllClothing() } returns flowOf(items)

        val result = filter.getCandidates(context, limit = 5)

        assertThat(result).hasSize(5)
    }
}
