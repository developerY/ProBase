package com.zoewave.ashbike.data.di

import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.data.repository.bike.BikeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BikeDataModule {

    @Binds
    @Singleton
    abstract fun bindBikeRepository(
        impl: BikeRepositoryImpl
    ): BikeRepository
}