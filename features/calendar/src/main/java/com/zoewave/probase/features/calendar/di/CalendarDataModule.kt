package com.zoewave.probase.features.calendar.di

import android.content.Context
import androidx.room3.Room
import com.zoewave.probase.features.calendar.data.db.CalendarSyncDatabase
import com.zoewave.probase.features.calendar.data.db.dao.CalendarSyncDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CalendarDataModule {

    @Provides
    @Singleton
    fun provideCalendarSyncDatabase(
        @ApplicationContext context: Context
    ): CalendarSyncDatabase {
        return Room.databaseBuilder(
            context,
            CalendarSyncDatabase::class.java,
            "calendar_sync.db"
        ).build()
    }

    @Provides
    fun provideCalendarSyncDao(database: CalendarSyncDatabase): CalendarSyncDao {
        return database.calendarSyncDao()
    }
}
