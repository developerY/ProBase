package com.zoewave.probase.seaweed.features.spendingcontrol.domain

import com.zoewave.probase.seaweed.model.SpendingType
import javax.inject.Inject

class RulesBasedClassifier @Inject constructor() : TransactionClassifier {
    
    override suspend fun classify(context: AuthContext): ClassificationResult {
        // Mock rules-based classification
        val categoryId = when {
            context.merchantName.contains("Starbucks", ignoreCase = true) -> "dining_id"
            context.merchantName.contains("Amazon", ignoreCase = true) -> "shopping_id"
            context.merchantName.contains("Rent", ignoreCase = true) -> "housing_id"
            else -> "misc_id"
        }

        val type = when (categoryId) {
            "housing_id", "utilities_id", "food_id" -> SpendingType.NEED
            else -> SpendingType.WANT
        }

        return ClassificationResult(categoryId, type)
    }
}
