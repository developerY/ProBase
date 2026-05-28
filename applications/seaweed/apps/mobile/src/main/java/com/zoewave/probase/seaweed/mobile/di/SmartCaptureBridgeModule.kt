package com.zoewave.probase.seaweed.mobile.di

import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SmartCaptureBridgeModule {

    @Provides
    @Singleton
    fun provideDefaultSmartCaptureSettings(
        @Named("Seaweed") impl: SmartCaptureSettings
    ): SmartCaptureSettings = impl
}
