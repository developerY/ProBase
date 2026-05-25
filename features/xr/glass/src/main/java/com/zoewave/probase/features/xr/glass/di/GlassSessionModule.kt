package com.zoewave.probase.features.xr.glass.di

import com.zoewave.probase.features.xr.glass.data.GlassSessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlassSessionModule {
    @Provides
    @Singleton
    fun provideGlassSessionRepository(): GlassSessionRepository {
        return GlassSessionRepository()
    }
}
