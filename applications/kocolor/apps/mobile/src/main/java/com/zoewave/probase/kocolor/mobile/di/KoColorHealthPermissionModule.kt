package com.zoewave.probase.kocolor.mobile.di

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
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
 * KoColor's Health Connect permission set: the deliberate minimum-scope baseline.
 *
 * KoColor strips Health Connect permissions from its manifest (`tools:node="remove"`) and
 * only ever reads the wellness baseline (steps, calories, sleep, hydration) for its summary
 * UI. This binding intentionally duplicates [DefaultHealthPermissionPolicy] rather than
 * relying on it, so future default changes can't silently widen KoColor's scope and the next
 * Play-policy review lands here, in KoColor's own module.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class KoColorHealthPermissionModule {

    @Binds
    @Singleton
    abstract fun bindHealthPermissionPolicy(
        impl: KoColorHealthPermissionPolicy
    ): AppHealthPermissionPolicy
}

@Singleton
class KoColorHealthPermissionPolicy @Inject constructor() : AppHealthPermissionPolicy {
    override val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class)
    )
}
