package com.zoewave.probase.gotmind.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.zoewave.probase.gotmind.database.dao.ScoreDao
import com.zoewave.probase.gotmind.database.dao.MemBloxScoreDao

@Database(entities = [ScoreEntity::class, MemBloxScoreEntity::class], version = 2)
abstract class GotMindDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun membloxScoreDao(): MemBloxScoreDao
}
