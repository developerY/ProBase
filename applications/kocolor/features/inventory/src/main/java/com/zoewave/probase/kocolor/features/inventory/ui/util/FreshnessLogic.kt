package com.zoewave.probase.kocolor.features.inventory.ui.util

import androidx.compose.ui.graphics.Color
import com.zoewave.probase.core.model.ritual.ClothingItem

enum class FreshnessState {
    RESTING, FRESH, IN_ROTATION
}

/**
 * Deterministic precedence for freshness states:
 * 1. RESTING: Used < 48h (Binary Cooldown).
 * 2. FRESH: Never used OR used > 10 days ago.
 * 3. IN ROTATION: Default fallback.
 */
fun ClothingItem.getFreshnessState(): FreshnessState {
    val now = System.currentTimeMillis()
    val fortyEightHours = 48 * 60 * 60 * 1000L
    val tenDays = 10 * 24 * 60 * 60 * 1000L

    val lastUsed = lastUsedTimestamp ?: 0L
    val diff = now - lastUsed

    return when {
        lastUsed > 0 && diff < fortyEightHours -> FreshnessState.RESTING
        usageCount == 0 || diff >= tenDays -> FreshnessState.FRESH
        else -> FreshnessState.IN_ROTATION
    }
}

val FreshnessState.color: Color
    get() = when (this) {
        FreshnessState.RESTING -> Color(0xFF5A3854) // Muted Plum
        FreshnessState.FRESH -> Color(0xFF00BCD4)   // Cyan
        FreshnessState.IN_ROTATION -> Color(0xFFD4AF37) // Gold
    }
