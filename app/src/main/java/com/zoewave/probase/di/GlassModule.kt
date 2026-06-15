package com.zoewave.probase.di

import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.data.repository.RitualRepository
import com.zoewave.probase.data.repository.DefaultGlassBridgeRepository
import com.zoewave.probase.data.repository.DefaultRitualRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GlassModule {

    @Binds
    @Singleton
    abstract fun bindGlassBridgeRepository(
        impl: DefaultGlassBridgeRepository
    ): GlassBridgeRepository

    @Binds
    @Singleton
    abstract fun bindRitualRepository(
        impl: DefaultRitualRepository
    ): RitualRepository
}
