package com.zoewave.probase.features.health.core.di

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-app Health Connect permission set.
 *
 * Every app that uses the shared health UI binds its own policy in its app module, so a
 * Play-policy scoping change in one app can never silently break another app's sync again
 * (Oct 2026: KoColor's minimum-scope trim of the previously-shared hardcoded set broke
 * AshBike ride sync). The bound set drives both the pre-flight check (`hasAllPermissions`)
 * and the permission prompt, so it must 1:1 cover every record type the app reads or writes:
 * a type that is requested nowhere can be granted nowhere.
 */
interface AppHealthPermissionPolicy {
    val permissions: Set<String>
}

/**
 * Fallback policy for apps that use the shared health UI without binding their own set.
 * This is the historical wellness baseline (reads + hydration write only); it intentionally
 * does NOT cover ride sync writes. Apps with real sync flows must bind their own policy.
 */
@Singleton
class DefaultHealthPermissionPolicy @Inject constructor() : AppHealthPermissionPolicy {
    override val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class)
    )
}

/**
 * Resolved policy: the app-bound [AppHealthPermissionPolicy] when one exists, else the
 * [DefaultHealthPermissionPolicy]. The wrapper avoids duplicate-binding issues between the
 * library default and app overrides.
 */
class ResolvedHealthPermissionPolicy(
    val permissions: Set<String>
)

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthPermissionPolicyModule {

    @BindsOptionalOf
    abstract fun bindOptionalPolicy(): AppHealthPermissionPolicy

    companion object {
        @Provides
        @Singleton
        fun provideResolvedPolicy(
            customPolicy: Optional<AppHealthPermissionPolicy>,
            defaultPolicy: DefaultHealthPermissionPolicy
        ): ResolvedHealthPermissionPolicy {
            return ResolvedHealthPermissionPolicy(
                customPolicy.map { it.permissions }.orElse(defaultPolicy.permissions)
            )
        }
    }
}
