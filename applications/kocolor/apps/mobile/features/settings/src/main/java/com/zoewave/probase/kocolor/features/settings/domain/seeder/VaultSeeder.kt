package com.zoewave.probase.kocolor.features.settings.domain.seeder

interface VaultSeeder {
    suspend fun wipeAndSeedDatabase(): Result<Unit>
}
