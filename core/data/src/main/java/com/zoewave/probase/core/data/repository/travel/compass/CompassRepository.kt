package com.zoewave.probase.core.data.repository.travel.compass

import kotlinx.coroutines.flow.Flow

interface CompassRepository {
    val headingFlow: Flow<Float> // heading in degrees, 0 = North, 90 = East, etc.
}
