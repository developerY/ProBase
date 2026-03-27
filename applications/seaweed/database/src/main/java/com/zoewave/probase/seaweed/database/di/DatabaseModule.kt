package com.zoewave.probase.seaweed.database.di

import android.content.Context
import androidx.room.Room
import com.zoewave.probase.seaweed.database.SeaweedDatabase
import com.zoewave.probase.seaweed.database.TransactionDao
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
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: SeaweedDatabase): TransactionDao =
        db.transactionDao
}
