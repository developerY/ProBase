package com.zoewave.probase.features.health.core.domain

import com.zoewave.probase.features.health.core.SkinInsight

data class HealthSummary(
    val hasPermissions: Boolean,
    val sleepHours: Float? = null,
    val sleepDurationLabel: String? = null,
    val hydrationLiters: Double = 0.0,
    val insights: List<SkinInsight> = emptyList()
)
