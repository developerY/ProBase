package com.zoewave.ashbike.data.di

import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.data.repository.bike.BikeRepositoryImpl
import com.zoewave.probase.ashbike.data.repository.FakeRitualRepository
import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.data.repository.LiveAiRepository
import com.zoewave.probase.core.data.repository.RitualRepository
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

    @Binds
    @Singleton
    abstract fun bindRitualRepository(
        impl: FakeRitualRepository
    ): RitualRepository

    @Binds
    @Singleton
    abstract fun bindGlassBridgeRepository(
        impl: FakeRitualRepository
    ): GlassBridgeRepository

    @Binds
    @Singleton
    abstract fun bindLiveAiRepository(
        impl: FakeRitualRepository
    ): LiveAiRepository
}
