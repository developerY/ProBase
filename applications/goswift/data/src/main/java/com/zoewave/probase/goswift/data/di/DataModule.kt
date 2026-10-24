package com.zoewave.probase.goswift.data.di

import com.zoewave.probase.goswift.data.caffeine.CaffeineAdvisoryAiService
import com.zoewave.probase.goswift.data.caffeine.CaffeineDecayEngine
import com.zoewave.probase.goswift.data.caffeine.DeterministicAdvisoryFallback
import com.zoewave.probase.goswift.data.caffeine.DeterministicCaffeineDecayEngine
import com.zoewave.probase.goswift.data.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun provideCaffeineAdvisoryAiService(): CaffeineAdvisoryAiService {
        return DeterministicAdvisoryFallback()
    }
}

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

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: ThemeRepositoryImpl
    ): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindAiConfigurationSettings(
        impl: GoSwiftAiSettings
    ): com.zoewave.probase.core.data.repository.AiConfigurationSettings

    @Binds
    @Singleton
    abstract fun bindSmartCaptureSettings(
        impl: GoSwiftAiSettings
    ): com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
}
