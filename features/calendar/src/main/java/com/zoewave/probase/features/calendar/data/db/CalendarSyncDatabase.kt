package com.zoewave.probase.features.calendar.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.zoewave.probase.features.calendar.data.db.dao.CalendarSyncDao
import com.zoewave.probase.features.calendar.data.db.entity.CalendarSyncEntity

@Database(
    entities = [CalendarSyncEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CalendarSyncDatabase : RoomDatabase() {
    abstract fun calendarSyncDao(): CalendarSyncDao
}
