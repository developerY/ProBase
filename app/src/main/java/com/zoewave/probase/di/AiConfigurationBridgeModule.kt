package com.zoewave.probase.di

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
        impl: FakeSmartCaptureSettings
    ): AiConfigurationSettings
}
