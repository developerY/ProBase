package com.zoewave.probase.di

import com.zoewave.probase.features.smartcapture.domain.SmartCaptureSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmartCaptureBridgeModule {

    @Binds
    @Singleton
    abstract fun bindSmartCaptureSettings(
        impl: FakeSmartCaptureSettings
    ): SmartCaptureSettings
}
