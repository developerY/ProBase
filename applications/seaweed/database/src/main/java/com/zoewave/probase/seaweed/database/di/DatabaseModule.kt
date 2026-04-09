package com.zoewave.probase.seaweed.database.di

import android.content.Context
import androidx.room3.Room
import com.zoewave.probase.seaweed.database.SeaweedDatabase
import com.zoewave.probase.seaweed.database.TransactionDao
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
    fun provideSeaweedDatabase(@ApplicationContext ctx: Context): SeaweedDatabase =
        Room.databaseBuilder(ctx, SeaweedDatabase::class.java, SeaweedDatabase.DATABASE_NAME)
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: SeaweedDatabase): TransactionDao =
        db.transactionDao

    @Provides
    @Singleton
    fun provideRecurringExpenseDao(db: SeaweedDatabase) = db.recurringExpenseDao

    @Provides
    @Singleton
    fun provideUserSettingsDao(db: SeaweedDatabase) = db.userSettingsDao

    @Provides
    @Singleton
    fun provideBudgetTargetDao(db: SeaweedDatabase) = db.budgetTargetDao
}
