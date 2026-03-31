package com.zoewave.probase.goswift.database.di

import android.content.Context
import androidx.room3.Room
import com.zoewave.probase.goswift.database.GoSwiftDatabase
import com.zoewave.probase.goswift.database.ShotDao
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
    fun provideGoSwiftDatabase(
        @ApplicationContext context: Context
    ): GoSwiftDatabase {
        return Room.databaseBuilder(
            context,
            GoSwiftDatabase::class.java,
            "goswift-database"
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Provides
    fun provideShotDao(database: GoSwiftDatabase): ShotDao {
        return database.shotDao()
    }
}
