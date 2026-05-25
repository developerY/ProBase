package com.zoewave.probase.kocolor.mobile.di

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
object SettingsBridgeModule {

    @Provides
    @Singleton
    fun provideDefaultAiSettings(
        @Named("KoColor") impl: AiConfigurationSettings
    ): AiConfigurationSettings = impl

    @Provides
    @Singleton
    fun provideDefaultSmartCaptureSettings(
        @Named("KoColor") impl: SmartCaptureSettings
    ): SmartCaptureSettings = impl
}
