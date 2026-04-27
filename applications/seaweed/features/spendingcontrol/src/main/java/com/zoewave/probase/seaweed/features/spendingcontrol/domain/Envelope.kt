package com.zoewave.probase.seaweed.features.spendingcontrol.domain

import kotlinx.serialization.Serializable

/**
 * Represents a spending boundary for a set of categories.
 */
@Serializable
data class Envelope(
    val id: String,
    val name: String,
    val monthlyLimitCents: Long,
    val currentSpentCents: Long,
    val categoryIds: List<String>,
    val isEnabled: Boolean = true,
    val priority: EnvelopePriority = EnvelopePriority.NORMAL
)

enum class EnvelopePriority {
    CRITICAL, // Hard enforcement, essential needs
    NORMAL,   // Standard enforcement
    FLEXIBLE  // Soft friction, discretionary wants
}
