package com.zoewave.probase.kocolor.features.settings.data.seeder

import com.zoewave.probase.kocolor.features.settings.domain.seeder.VaultSeeder
import javax.inject.Inject

class ReleaseVaultSeeder @Inject constructor() : VaultSeeder {
    override suspend fun wipeAndSeedDatabase(): Result<Unit> {
        // No-Op for release builds
        return Result.success(Unit)
    }
}
