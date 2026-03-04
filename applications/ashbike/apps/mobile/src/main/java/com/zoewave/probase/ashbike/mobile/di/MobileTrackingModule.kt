package com.zoewave.probase.ashbike.mobile.di
import com.zoewave.ashbike.data.services.RideTrackingEngine
import com.zoewave.probase.ashbike.mobile.data.sensor.MobileLocationBleEngine

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MobileTrackingModule {

    @Binds
    @Singleton
    abstract fun bindTrackingEngine(
        impl: MobileLocationBleEngine
    ): RideTrackingEngine
}