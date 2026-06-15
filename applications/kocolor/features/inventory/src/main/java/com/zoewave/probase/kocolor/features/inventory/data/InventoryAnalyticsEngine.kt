package com.zoewave.probase.kocolor.features.inventory.data

import com.zoewave.probase.core.model.ritual.CosmeticItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for calculating advanced inventory metrics and demand forecasting.
 */
@Singleton
class InventoryAnalyticsEngine @Inject constructor() {

    /**
     * Calculates the Cost-Per-Use (CPU) for an item.
     */
    fun calculateCostPerUse(item: CosmeticItem): Double? {
        return item.costPerUse
    }

    /**
     * Calculates total spending across a collection of items.
     */
    fun calculateTotalSpending(items: List<CosmeticItem>): Double {
        return items.filter { !it.isArchived }.sumOf { it.price ?: 0.0 }
    }

    /**
     * Identifies products nearing expiration.
     */
    fun getExpiringSoon(items: List<CosmeticItem>, thresholdDays: Int = 30): List<CosmeticItem> {
        val now = System.currentTimeMillis()
        val thresholdMillis = thresholdDays * 24 * 60 * 60 * 1000L
        return items.filter { item ->
            val expiry = item.estimatedExpiry
            expiry != null && (expiry - now) < thresholdMillis
        }
    }

    /**
     * Forecasts replenishment date based on usage velocity.
     * Simple linear model: average usage per day.
     */
    fun forecastReplenishmentDate(item: CosmeticItem, totalUsesPossible: Int): Long? {
        if (item.usageCount <= 0 || item.timestamp <= 0) return null
        
        val daysSinceAdded = (System.currentTimeMillis() - item.timestamp) / (24 * 60 * 60 * 1000f)
        if (daysSinceAdded <= 0) return null
        
        val usagePerDay = item.usageCount / daysSinceAdded
        if (usagePerDay <= 0) return null
        
        val remainingUses = totalUsesPossible - item.usageCount
        if (remainingUses <= 0) return System.currentTimeMillis()
        
        val daysToReplenishment = (remainingUses / usagePerDay).toLong()
        return System.currentTimeMillis() + (daysToReplenishment * 24 * 60 * 60 * 1000L)
    }
}
