package com.zoewave.probase.goswift.data.di

import com.zoewave.probase.goswift.data.caffeine.CaffeineDecayEngine
import com.zoewave.probase.goswift.data.caffeine.DeterministicCaffeineDecayEngine
import com.zoewave.probase.goswift.data.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindCaffeineDecayEngine(
        engine: DeterministicCaffeineDecayEngine
    ): CaffeineDecayEngine

    @Binds
    @Singleton
    abstract fun bindShotRepository(
        impl: ShotRepositoryImpl
    ): ShotRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(
        impl: HealthRepositoryImpl
    ): HealthRepository

    @Binds
    @Singleton
    abstract fun bindHydrationRepository(
        impl: HydrationRepositoryImpl
    ): HydrationRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(
        impl: NutritionRepositoryImpl
    ): NutritionRepository
}
