package com.zoewave.probase.ashbike.mobile.di

import com.zoewave.probase.core.data.repository.sensor.heart.BleHeartRateRepository
import com.zoewave.probase.core.data.repository.sensor.heart.HeartRateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MobileSensorModule {

    @Binds
    abstract fun bindHeartRateRepository(
        impl: BleHeartRateRepository
    ): HeartRateRepository
}