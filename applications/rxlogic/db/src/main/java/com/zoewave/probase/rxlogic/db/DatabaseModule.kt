package com.zoewave.probase.rxlogic.db

import android.content.Context
import androidx.room3.Room
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
    fun provideRxLogicDatabase(
        @ApplicationContext context: Context
    ): RxLogicDatabase {
        return Room.databaseBuilder(
            context,
            RxLogicDatabase::class.java,
            "rx_logic.db"
        ).build()
    }

    @Provides
    fun provideMedicationDao(database: RxLogicDatabase): MedicationDao {
        return database.medicationDao()
    }
}
