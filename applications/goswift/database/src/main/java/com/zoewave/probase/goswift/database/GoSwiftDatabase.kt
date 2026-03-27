package com.zoewave.probase.goswift.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ShotEntity::class], version = 1)
abstract class GoSwiftDatabase : RoomDatabase() {
    abstract fun shotDao(): ShotDao
}
