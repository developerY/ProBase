package com.zoewave.probase.features.smartcapture.di

import com.zoewave.probase.features.smartcapture.data.CloudCaptureEngineImpl
import com.zoewave.probase.features.smartcapture.data.FakeSmartCaptureSettings
import com.zoewave.probase.features.smartcapture.data.LocalCaptureEngineImpl
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureEngine
import com.zoewave.probase.features.smartcapture.domain.SmartCaptureSettings
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

    @Binds
    @Singleton
    abstract fun bindSettings(impl: FakeSmartCaptureSettings): SmartCaptureSettings
}
