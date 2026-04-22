package com.zoewave.probase.features.health.cgm.data.repository

import com.zoewave.probase.core.data.security.SecureTokenManager
import com.zoewave.probase.core.model.health.GlucoseReading
import com.zoewave.probase.core.model.health.GlucoseSource
import com.zoewave.probase.core.model.health.GlucoseType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GlucoseRepository for Medtrum.
 * Medium Accessibility: Cloud based sync.
 */
@Singleton
class MedtrumRepository @Inject constructor(
    private val secureTokenManager: SecureTokenManager
) : GlucoseRepository {

    override val glucoseReadings: Flow<GlucoseReading> = flow {
        while (true) {
            emit(
                GlucoseReading(
                    valueMgDl = (115..165).random().toFloat(),
                    timestamp = Instant.now(),
                    source = GlucoseSource.MEDTRUM,
                    type = GlucoseType.CGM,
                    trendArrow = "FLAT"
                )
            )
            delay(300000)
        }
    }

    override suspend fun scanSensor() {}
}
