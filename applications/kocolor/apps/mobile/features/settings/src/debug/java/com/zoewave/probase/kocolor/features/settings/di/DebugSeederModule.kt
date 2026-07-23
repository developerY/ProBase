package com.zoewave.probase.kocolor.features.settings.di

import com.zoewave.probase.kocolor.features.settings.data.seeder.LocalAssetVaultSeeder
import com.zoewave.probase.kocolor.features.settings.domain.seeder.VaultSeeder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugSeederModule {

    @Binds
    @Singleton
    abstract fun bindVaultSeeder(
        impl: LocalAssetVaultSeeder
    ): VaultSeeder
}
