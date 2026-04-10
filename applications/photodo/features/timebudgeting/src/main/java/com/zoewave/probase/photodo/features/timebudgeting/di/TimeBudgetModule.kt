package com.zoewave.probase.photodo.features.timebudgeting.di

import android.content.Context
import androidx.room3.Room
import com.zoewave.probase.photodo.features.timebudgeting.data.db.TimeBudgetDatabase
import com.zoewave.probase.photodo.features.timebudgeting.data.db.dao.TimeBudgetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object TimeBudgetModule {

    @Provides
    @Singleton
    fun provideTimeBudgetDatabase(
        @ApplicationContext context: Context
    ): TimeBudgetDatabase {
        return Room.databaseBuilder(
            context,
            TimeBudgetDatabase::class.java,
            "time_budgeting.db"
        ).build()
    }

    @Provides
    fun provideTimeBudgetDao(database: TimeBudgetDatabase): TimeBudgetDao {
        return database.timeBudgetDao()
    }
}
