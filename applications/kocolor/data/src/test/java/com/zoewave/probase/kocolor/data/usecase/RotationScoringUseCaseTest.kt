package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.kocolor.data.repository.RotationRepository
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RotationScoringUseCaseTest {

    private val repository: RotationRepository = mockk()
    private lateinit var useCase: RotationScoringUseCase

    @Before
    fun setup() {
        useCase = RotationScoringUseCase(repository)
    }

    @Test
    fun `when global outfits less than 5, penalty is always 0 (Cold Start Rule)`() = runTest {
        // Given
        coEvery { repository.observeGlobalMetrics() } returns flowOf(GlobalRotationMetricsEntity(totalOutfitsCommitted = 3))
        
        // When
        val penalty = useCase.calculateRotationPenalty("item1", "TOPS")
        
        // Then
        assertThat(penalty).isEqualTo(0.0)
    }

    @Test
    fun `when item used recently (within 48h), penalty is 1_0`() = runTest {
        // Given
        coEvery { repository.observeGlobalMetrics() } returns flowOf(GlobalRotationMetricsEntity(totalOutfitsCommitted = 10))
        val recentUsage = ClothingUsageEntity(
            productId = "item1", 
            rotationCategoryId = "TOPS",
            useCount = 1, 
            lastUsedTimestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
        )
        coEvery { repository.getUsageForCategory("TOPS") } returns listOf(recentUsage)
        
        // When
        val penalty = useCase.calculateRotationPenalty("item1", "TOPS")
        
        // Then
        assertThat(penalty).isEqualTo(1.0)
    }

    @Test
    fun `when item usage share exceeds 35 percent, penalty is 1_0`() = runTest {
        // Given
        coEvery { repository.observeGlobalMetrics() } returns flowOf(GlobalRotationMetricsEntity(totalOutfitsCommitted = 10))
        val highUsageItem = ClothingUsageEntity(productId = "item1", rotationCategoryId = "TOPS", useCount = 40) // 40% of 100
        val otherItems = (2..4).map { ClothingUsageEntity(productId = "item$it", rotationCategoryId = "TOPS", useCount = 20) }
        
        coEvery { repository.getUsageForCategory("TOPS") } returns (listOf(highUsageItem) + otherItems)
        
        // When
        val penalty = useCase.calculateRotationPenalty("item1", "TOPS")
        
        // Then
        assertThat(penalty).isEqualTo(1.0)
    }

    @Test
    fun `when item has low usage and is not recent, penalty is proportional`() = runTest {
        // Given
        coEvery { repository.observeGlobalMetrics() } returns flowOf(GlobalRotationMetricsEntity(totalOutfitsCommitted = 10))
        // Total usage = 100. Item1 usage = 10. Share = 10%. Threshold = 35%. Expected = 10/35 = 0.285
        val targetItem = ClothingUsageEntity(
            productId = "item1", 
            rotationCategoryId = "TOPS",
            useCount = 10, 
            lastUsedTimestamp = System.currentTimeMillis() - (100 * 3600000) // 100 hours ago (>48h)
        )
        val others = listOf(ClothingUsageEntity(productId = "item2", rotationCategoryId = "TOPS", useCount = 90))
        
        coEvery { repository.getUsageForCategory("TOPS") } returns (listOf(targetItem) + others)
        
        // When
        val penalty = useCase.calculateRotationPenalty("item1", "TOPS")
        
        // Then
        assertThat(penalty).isAtLeast(0.28)
        assertThat(penalty).isAtMost(0.29)
    }
}
