package com.zoewave.probase.core.data.di

import com.zoewave.probase.core.data.repository.DefaultHydrationSettings
import com.zoewave.probase.core.data.repository.HydrationSettings
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultHydration

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppHydration

@Module
@InstallIn(SingletonComponent::class)
interface HydrationModule {

    @Binds
    @Singleton
    @DefaultHydration
    fun bindsDefaultHydrationSettings(
        impl: DefaultHydrationSettings
    ): HydrationSettings

    @BindsOptionalOf
    @AppHydration
    fun optionalAppHydrationSettings(): HydrationSettings

    companion object {
        @Provides
        @Singleton
        fun provideHydrationSettings(
            @AppHydration optionalAppSettings: Optional<HydrationSettings>,
            @DefaultHydration defaultSettings: HydrationSettings
        ): HydrationSettings {
            return if (optionalAppSettings.isPresent) {
                optionalAppSettings.get()
            } else {
                defaultSettings
            }
        }
    }
}
