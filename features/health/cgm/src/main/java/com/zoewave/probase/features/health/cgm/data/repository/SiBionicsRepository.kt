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
 * Implementation of GlucoseRepository for SiBionics.
 * Medium Accessibility: Uses community-driven Bluetooth interception or API.
 */
@Singleton
class SiBionicsRepository @Inject constructor(
    private val secureTokenManager: SecureTokenManager
) : GlucoseRepository {

    override val glucoseReadings: Flow<GlucoseReading> = flow {
        while (true) {
            emit(
                GlucoseReading(
                    valueMgDl = (105..155).random().toFloat(),
                    timestamp = Instant.now(),
                    source = GlucoseSource.SIBIONICS,
                    type = GlucoseType.CGM,
                    trendArrow = "RISING"
                )
            )
            delay(300000)
        }
    }

    override suspend fun scanSensor() {
        // Bluetooth based, might trigger a reconnect.
    }
}
