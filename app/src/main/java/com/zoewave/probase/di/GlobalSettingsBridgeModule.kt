package com.zoewave.probase.di

import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlobalSettingsBridgeModule {

    @Provides
    @Singleton
    fun provideDefaultAiSettings(
        @Named("PhotoDo") impl: AiConfigurationSettings
    ): AiConfigurationSettings = impl

    @Provides
    @Singleton
    fun provideDefaultSmartCaptureSettings(
        @Named("PhotoDo") impl: SmartCaptureSettings
    ): SmartCaptureSettings = impl
}
