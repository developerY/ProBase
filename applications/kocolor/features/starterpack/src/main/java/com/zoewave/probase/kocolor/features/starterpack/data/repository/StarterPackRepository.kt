package com.zoewave.probase.kocolor.features.starterpack.data.repository

import kotlinx.coroutines.flow.Flow

interface StarterPackRepository {
    suspend fun ingestStarterPack(): Result<Unit>
    suspend fun wipeStarterPack(): Result<Unit>
}
