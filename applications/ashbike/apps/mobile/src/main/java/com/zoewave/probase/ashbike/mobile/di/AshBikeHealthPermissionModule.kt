package com.zoewave.probase.ashbike.mobile.di

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import com.zoewave.probase.features.health.core.di.AppHealthPermissionPolicy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AshBike's Health Connect permission set.
 *
 * Each app binds its own [AppHealthPermissionPolicy] so Play-policy scoping in one app can never
 * silently break another app's sync again (see Oct 2026: KoColor's minimum-scope trim of the
 * previously-shared set broke AshBike ride sync). This set must 1:1 cover every record type
 * AshBike reads or writes:
 * - Ride sync (`SyncRideUseCase`): writes ExerciseSession, Distance, TotalCaloriesBurned,
 *   HeartRate.
 * - Dashboard load (`HealthViewModel.initialLoad`): reads Exercise, Steps, Distance,
 *   Total/ActiveCalories, Sleep, Hydration, HeartRate.
 * Guarded by [AshBikeHealthPermissionPolicyTest].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AshBikeHealthPermissionModule {

    @Binds
    @Singleton
    abstract fun bindHealthPermissionPolicy(
        impl: AshBikeHealthPermissionPolicy
    ): AppHealthPermissionPolicy
}

@Singleton
class AshBikeHealthPermissionPolicy @Inject constructor() : AppHealthPermissionPolicy {
    override val permissions: Set<String> = setOf(
        
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        
        
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class)
    )
}
