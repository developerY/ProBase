package com.zoewave.probase.features.health.core.domain

import com.zoewave.probase.core.data.service.health.HealthSessionManager
import javax.inject.Inject

class LogHydrationUseCase @Inject constructor(
    private val healthSessionManager: HealthSessionManager
) {
    suspend operator fun invoke(volumeLiters: Double) {
        healthSessionManager.insertHydration(volumeLiters)
    }
}
