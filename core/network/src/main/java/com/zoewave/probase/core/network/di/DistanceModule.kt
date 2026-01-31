package com.zoewave.probase.core.network.di

import com.zoewave.probase.core.network.repository.travel.DistanceRepository
import com.zoewave.probase.core.network.repository.travel.DistanceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DistanceModule {

    @Binds
    @Singleton
    abstract fun bindDistanceRepository(
        impl: DistanceRepositoryImpl
    ): DistanceRepository
}
