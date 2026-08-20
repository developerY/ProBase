package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.kocolor.data.repository.RotationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RotationScoringUseCase @Inject constructor(
    private val rotationRepository: RotationRepository
) {
    // Thresholds and Policy Constants
    private val minimumOutfitsForPenalty = 5L 
    private val highFrequencyShare = 0.35 // Item used in >35% of category selections
    private val maximumRecencyPenaltyWindowMs = 48 * 60 * 60 * 1000L // 48h

    /**
     * Calculates a normalized rotation penalty [0.0 to 1.0] based on usage frequency
     * and recency within a category.
     */
    suspend fun calculateRotationPenalty(productId: String, categoryId: String): Double {
        // 1. Cold Start Rule: check global history
        val globalMetrics = rotationRepository.observeGlobalMetrics().first()
        if ((globalMetrics?.totalOutfitsCommitted ?: 0L) < minimumOutfitsForPenalty) {
            return 0.0
        }

        // 2. Fetch Category Usage State (Joins at DAO level)
        val allItemsInCategory = rotationRepository.getUsageForCategory(categoryId)
        val targetItem = allItemsInCategory.find { it.productId == productId }
            ?: return 0.0 // No usage history yet

        val totalCategoryUsage = allItemsInCategory.sumOf { it.useCount }
        if (totalCategoryUsage == 0L) return 0.0

        // 3. Derived Metrics calculation
        val currentUsageShare = targetItem.useCount.toDouble() / totalCategoryUsage
        val currentTime = System.currentTimeMillis()
        val recencyMs = targetItem.lastUsedTimestamp?.let { currentTime - it } ?: Long.MAX_VALUE

        // 4. Transform to scoring factors
        val frequencyPenalty = if (currentUsageShare > highFrequencyShare) 1.0 
                             else (currentUsageShare / highFrequencyShare)
        
        val recencyPenalty = if (recencyMs < maximumRecencyPenaltyWindowMs) 1.0 
                           else 0.0

        // Combine factors: Recency is a heavy immediate penalty, frequency is long-term.
        return maxOf(frequencyPenalty, recencyPenalty).coerceIn(0.0, 1.0)
    }
}
