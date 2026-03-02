package com.zoewave.probase.ashbike.wear.data.health.di

import android.content.Context
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import com.zoewave.ashbike.data.services.RideTrackingEngine
import com.zoewave.probase.ashbike.wear.data.health.sensor.WearExerciseClientEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WearTrackingModule {

    // 1. Bind the Interface to the Wear OS Implementation
    @Binds
    @Singleton
    abstract fun bindTrackingEngine(
        impl: WearExerciseClientEngine
    ): RideTrackingEngine

    // 2. Provide the actual Google Health Services ExerciseClient
    companion object {
        @Provides
        @Singleton
        fun provideExerciseClient(
            @ApplicationContext context: Context
        ): ExerciseClient {
            return HealthServices.getClient(context).exerciseClient
        }
    }
}