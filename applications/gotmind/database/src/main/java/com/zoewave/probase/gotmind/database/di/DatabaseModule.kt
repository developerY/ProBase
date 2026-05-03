package com.zoewave.probase.gotmind.database.di

import android.content.Context
import androidx.room3.Room
import com.zoewave.probase.gotmind.database.GotMindDatabase
import com.zoewave.probase.gotmind.database.dao.ScoreDao
import com.zoewave.probase.gotmind.database.dao.MemBloxScoreDao
import com.zoewave.probase.gotmind.database.dao.MindWaveScoreDao
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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
    fun provideGotMindDatabase(@ApplicationContext ctx: Context): GotMindDatabase =
        Room.databaseBuilder(ctx, GotMindDatabase::class.java, "gotmind.db")
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideScoreDao(db: GotMindDatabase): ScoreDao =
        db.scoreDao()

    @Provides
    @Singleton
    fun provideMemBloxScoreDao(db: GotMindDatabase): MemBloxScoreDao =
        db.membloxScoreDao()

    @Provides
    @Singleton
    fun provideMindWaveScoreDao(db: GotMindDatabase): MindWaveScoreDao =
        db.mindwaveScoreDao()
}
