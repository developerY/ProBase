package com.zoewave.probase.core.data.di

import com.zoewave.probase.core.data.repository.timer.TimerRepository
import com.zoewave.probase.core.data.repository.timer.TimerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds our TimerRepository interface to TimerRepositoryImpl
 * so you can `@Inject TimerRepository` anywhere.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TimerModule {
    @Binds
    @Singleton
    abstract fun bindTimerRepository(
        impl: TimerRepositoryImpl
    ): TimerRepository
}
