package com.zoewave.probase.di

import com.zoewave.probase.core.data.repository.DefaultHydrationSettings
import com.zoewave.probase.core.data.repository.HydrationSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHydrationSettings(
        impl: DefaultHydrationSettings
    ): HydrationSettings
}
