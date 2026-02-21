package com.zoewave.probase.ashbike.wear.di

import com.zoewave.probase.core.data.repository.sensor.WearHealthHeartRateRepository
import com.zoewave.probase.core.data.repository.sensor.heart.HeartRateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WearSensorModule {
    @Binds
    abstract fun bindHeartRateRepository(
        impl: WearHealthHeartRateRepository
    ): HeartRateRepository
}