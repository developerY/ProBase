package com.zoewave.probase.applications.journal.database.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.zoewave.probase.applications.journal.database.JournalDatabase
import com.zoewave.probase.applications.journal.database.dao.JournalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideJournalDatabase(@ApplicationContext context: Context): JournalDatabase {
        return Room.databaseBuilder(
            context,
            JournalDatabase::class.java,
            JournalDatabase.DATABASE_NAME
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Provides
    @Singleton
    fun provideJournalDao(db: JournalDatabase): JournalDao {
        return db.journalDao
    }
}
