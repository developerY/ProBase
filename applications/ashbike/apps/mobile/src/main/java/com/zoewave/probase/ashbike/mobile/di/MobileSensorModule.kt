package com.zoewave.probase.ashbike.mobile.di

import com.zoewave.ashbike.features.ble.BleSensorHub
import com.zoewave.ashbike.features.ble.RideSensorFusionEngine
import com.zoewave.probase.core.data.repository.sensor.heart.BleHeartRateRepository
import com.zoewave.probase.core.data.repository.sensor.heart.HeartRateRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MobileSensorModule provides Hilt bindings for AshBike phone sensors and BLE Hub.
 * Uses [BleSensorHub] and [RideSensorFusionEngine] for multi-sensor GATT telemetry fusion.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MobileSensorModule {

    @Binds
    abstract fun bindHeartRateRepository(
        impl: BleHeartRateRepository
    ): HeartRateRepository

    companion object {
        @Provides
        @Singleton
        fun provideBleSensorHub(): BleSensorHub = BleSensorHub()

        @Provides
        @Singleton
        fun provideRideSensorFusionEngine(): RideSensorFusionEngine = RideSensorFusionEngine()
    }
}
