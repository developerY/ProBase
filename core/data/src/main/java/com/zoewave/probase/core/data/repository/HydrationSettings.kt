package com.zoewave.probase.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Common settings interface for hydration goals.
 */
interface HydrationSettings {
    val hydrationGoalFlow: Flow<Double>
    suspend fun saveHydrationGoal(goal: Double)
}
