package com.zoewave.probase.gotmind.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.zoewave.probase.gotmind.database.dao.ScoreDao
import com.zoewave.probase.gotmind.database.dao.MemBloxScoreDao
import com.zoewave.probase.gotmind.database.dao.MindWaveScoreDao

@Database(entities = [ScoreEntity::class, MemBloxScoreEntity::class, MindWaveScoreEntity::class], version = 5)
abstract class GotMindDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun membloxScoreDao(): MemBloxScoreDao
    abstract fun mindwaveScoreDao(): MindWaveScoreDao
}
