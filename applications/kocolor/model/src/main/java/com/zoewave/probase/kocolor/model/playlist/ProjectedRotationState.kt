package com.zoewave.probase.kocolor.model.playlist

import java.time.Instant

/**
 * Pure domain model for simulating wear counts without touching historical data.
 */
data class ProjectedUsage(
    val useCount: Int,
    val lastUsedTimestamp: Long?
)

/**
 * Orchestrates the 'State Forwarding' for the 7-day style loop.
 * It allows the engine to penalize items that were 'picked' on Monday when generating Tuesday.
 */
class ProjectedRotationState(initialCommittedHistory: Map<String, ProjectedUsage>) {
    private val projectedHistory = initialCommittedHistory.toMutableMap()

    fun simulateWear(productId: String, simulatedWearTime: Instant) {
        val current = projectedHistory[productId]
        projectedHistory[productId] = ProjectedUsage(
            useCount = (current?.useCount ?: 0) + 1,
            lastUsedTimestamp = simulatedWearTime.toEpochMilli()
        )
    }

    fun getUsage(productId: String): ProjectedUsage? = projectedHistory[productId]

    fun getAllUsage(): Map<String, ProjectedUsage> = projectedHistory.toMap()
}
