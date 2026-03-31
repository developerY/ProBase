package com.zoewave.probase.seaweed.database

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class SeaweedDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao

    companion object {
        const val DATABASE_NAME = "seaweed_db"
    }
}
