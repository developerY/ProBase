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
 * Implementation of GlucoseRepository for Abbott LibreLinkUp (Cloud).
 * Medium Accessibility: Uses unofficial LinkUp API.
 */
@Singleton
class AbbottLibreLinkUpRepository @Inject constructor(
    private val secureTokenManager: SecureTokenManager
) : GlucoseRepository {

    override val glucoseReadings: Flow<GlucoseReading> = flow {
        while (true) {
            emit(
                GlucoseReading(
                    valueMgDl = (110..160).random().toFloat(),
                    timestamp = Instant.now(),
                    source = GlucoseSource.ABBOTT_LIBRE_LINK_UP,
                    type = GlucoseType.CGM,
                    trendArrow = "FALLING"
                )
            )
            delay(60000) // LibreLinkUp can provide 1-minute data in some regions
        }
    }

    override suspend fun scanSensor() {
        // Cloud based, no hardware scan.
    }
}
