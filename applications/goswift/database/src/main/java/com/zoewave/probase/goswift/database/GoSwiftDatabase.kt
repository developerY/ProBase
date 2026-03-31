package com.zoewave.probase.goswift.database

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(entities = [ShotEntity::class], version = 1)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class GoSwiftDatabase : RoomDatabase() {
    abstract fun shotDao(): ShotDao
}
