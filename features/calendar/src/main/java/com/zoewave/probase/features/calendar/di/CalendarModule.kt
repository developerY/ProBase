package com.zoewave.probase.features.calendar.di

import com.zoewave.probase.features.calendar.data.AndroidCalendarProvider
import com.zoewave.probase.features.calendar.domain.CalendarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CalendarModule {

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        impl: AndroidCalendarProvider
    ): CalendarRepository
}
