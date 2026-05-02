package com.zoewave.probase.gotmind.data.di

import com.zoewave.probase.gotmind.data.repository.GotMindRepository
import com.zoewave.probase.gotmind.data.repository.GotMindRepositoryImpl
import com.zoewave.probase.gotmind.data.repository.AppSettingsRepository
import com.zoewave.probase.gotmind.data.repository.AppSettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindGotMindRepository(
        impl: GotMindRepositoryImpl
    ): GotMindRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsRepositoryImpl
    ): AppSettingsRepository
}
