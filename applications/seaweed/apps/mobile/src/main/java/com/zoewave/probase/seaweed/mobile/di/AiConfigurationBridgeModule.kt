package com.zoewave.probase.seaweed.mobile.di

import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiConfigurationBridgeModule {

    @Provides
    @Singleton
    fun provideDefaultAiSettings(
        @Named("Seaweed") impl: AiConfigurationSettings
    ): AiConfigurationSettings = impl
}
