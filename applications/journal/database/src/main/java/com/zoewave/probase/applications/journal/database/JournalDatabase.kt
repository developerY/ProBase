package com.zoewave.probase.applications.journal.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.zoewave.probase.applications.journal.database.dao.JournalDao
import com.zoewave.probase.applications.journal.database.entity.JournalEntryEntity

@Database(entities = [JournalEntryEntity::class], version = 1, exportSchema = false)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class JournalDatabase : RoomDatabase() {
    abstract val journalDao: JournalDao

    companion object {
        const val DATABASE_NAME = "journal_db"
    }
}
