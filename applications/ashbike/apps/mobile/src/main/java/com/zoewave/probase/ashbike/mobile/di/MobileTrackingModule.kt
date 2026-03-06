package com.zoewave.probase.ashbike.mobile.di

import com.zoewave.ashbike.data.services.RideSyncEngine
import com.zoewave.ashbike.data.services.RideTrackingEngine
import com.zoewave.probase.ashbike.mobile.data.sensor.MobileLocationBleEngine
import com.zoewave.probase.ashbike.mobile.data.sync.MobileRideSyncEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface MobileTrackingModule {

    @Binds
    @Singleton
    fun bindTrackingEngine(
        impl: MobileLocationBleEngine
    ): RideTrackingEngine

    @Binds
    @Singleton
    fun bindRideSyncEngine(
        impl: MobileRideSyncEngine
    ): RideSyncEngine
}