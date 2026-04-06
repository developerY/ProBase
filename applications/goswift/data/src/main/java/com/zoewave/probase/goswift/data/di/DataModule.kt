package com.zoewave.probase.goswift.data.di

import com.zoewave.probase.goswift.data.HealthRepository
import com.zoewave.probase.goswift.data.HealthRepositoryImpl
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.data.ShotRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindShotRepository(
        shotRepositoryImpl: ShotRepositoryImpl
    ): ShotRepository

    @Binds
    fun bindHealthRepository(
        healthRepositoryImpl: HealthRepositoryImpl
    ): HealthRepository
}
