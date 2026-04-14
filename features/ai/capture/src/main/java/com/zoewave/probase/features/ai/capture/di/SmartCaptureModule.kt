package com.zoewave.probase.features.ai.capture.di

import com.zoewave.probase.features.ai.capture.data.CloudCaptureEngineImpl
import com.zoewave.probase.features.ai.capture.data.FakeSmartCaptureSettings
import com.zoewave.probase.features.ai.capture.data.LocalCaptureEngineImpl
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureEngine
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmartCaptureModule {

    @Binds
    @Singleton
    @Named("Cloud")
    abstract fun bindCloudEngine(impl: CloudCaptureEngineImpl): SmartCaptureEngine

    @Binds
    @Singleton
    @Named("Local")
    abstract fun bindLocalEngine(impl: LocalCaptureEngineImpl): SmartCaptureEngine
}
