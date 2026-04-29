package com.zoewave.probase.gotmind.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.zoewave.probase.gotmind.database.dao.ScoreDao

@Database(entities = [ScoreEntity::class], version = 1)
abstract class GotMindDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
}
