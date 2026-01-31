package com.zoewave.probase.core.data.di

import com.zoewave.probase.core.data.repository.travel.SpeedRepository
import com.zoewave.probase.core.data.repository.travel.SpeedRepositoryImpl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeedModule {

    @Binds
    @Singleton
    abstract fun bindSpeedRepository(
        impl: SpeedRepositoryImpl
    ): SpeedRepository
}
