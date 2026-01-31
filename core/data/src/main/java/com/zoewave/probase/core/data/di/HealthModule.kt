package com.zoewave.probase.core.data.di

import android.content.Context
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthModule {

    @Provides
    @Singleton
    fun provideHealthSessionManager(
        @ApplicationContext context: Context
    ): HealthSessionManager {
        return HealthSessionManager(context)
    }
}
