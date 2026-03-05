package com.zoewave.probase.ashbike.wear.data.health.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.zoewave.ashbike.data.services.RideSyncEngine
import com.zoewave.ashbike.data.services.RideTrackingEngine
import com.zoewave.probase.ashbike.wear.data.health.sensor.WearEmulatorTrackingEngine
import com.zoewave.probase.ashbike.wear.data.health.sensor.WearExerciseClientEngine
import com.zoewave.probase.ashbike.wear.data.sync.WearRideSyncEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WearTrackingModule {

    @Provides
    @Singleton
    fun provideExerciseClient(@ApplicationContext context: Context): ExerciseClient {
        return HealthServices.getClient(context).exerciseClient
    }

    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    @Provides
    @Singleton
    fun provideRideTrackingEngine(
        exerciseClient: ExerciseClient,
        fusedLocationClient: FusedLocationProviderClient
    ): RideTrackingEngine {

        // Check if we are running on an Android Studio Emulator
        val isEmulator = Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("sdk_gwear")

        return if (isEmulator) {
            // Inject the hacky hybrid engine so your mock routes work
            WearEmulatorTrackingEngine(exerciseClient, fusedLocationClient)
        } else {
            // Inject the pure, battery-optimized production engine
            WearExerciseClientEngine(exerciseClient)
        }
    }

    @Provides
    @Singleton
    fun provideRideSyncEngine(@ApplicationContext context: Context): RideSyncEngine {
        return WearRideSyncEngine(context)
    }
}