package com.zoewave.probase.core.data.di

import android.content.Context
import com.zoewave.probase.core.data.repository.alarm.AlarmRepository
import com.zoewave.probase.core.data.repository.alarm.AlarmRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    @Provides
    @Singleton
    fun provideAlarmRepository(
        @ApplicationContext context: Context
    ): AlarmRepository {
        return AlarmRepositoryImpl(context)
    }
}