package com.zoewave.probase.photodo.mobile.di

import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiConfigurationBridgeModule {

    @Binds
    @Singleton
    abstract fun bindAiConfigurationSettings(
        impl: AppSettingsRepository
    ): AiConfigurationSettings
}
