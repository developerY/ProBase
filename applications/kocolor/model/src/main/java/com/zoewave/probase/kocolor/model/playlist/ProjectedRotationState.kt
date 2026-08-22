package com.zoewave.probase.kocolor.model.playlist

import java.time.Instant

data class UsageSnapshot(
    val useCount: Int,
    val lastUsedAt: Long?
)

class ProjectedRotationState(initialCommittedHistory: Map<String, UsageSnapshot>) {
    private val projectedHistory = initialCommittedHistory.toMutableMap()

    fun simulateWear(productId: String, simulatedWearTime: Instant) {
        val current = projectedHistory[productId]
        projectedHistory[productId] = UsageSnapshot(
            useCount = (current?.useCount ?: 0) + 1,
            lastUsedAt = simulatedWearTime.toEpochMilli()
        )
    }

    fun getUsage(productId: String): UsageSnapshot? = projectedHistory[productId]

    fun getAllUsage(): Map<String, UsageSnapshot> = projectedHistory.toMap()
}
