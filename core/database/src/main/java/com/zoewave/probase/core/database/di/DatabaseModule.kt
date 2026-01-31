package com.zoewave.probase.core.database.di

import android.content.Context
import androidx.room.Room
import com.zoewave.probase.core.database.BaseProDB
import com.zoewave.probase.core.database.BaseProDao
import com.zoewave.probase.core.database.BaseProRepo
import com.zoewave.probase.core.database.BikeDatabase
import com.zoewave.probase.core.database.OtherDatabase
import com.zoewave.probase.core.database.repository.BaseProRepoImpl
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
    fun provideAppDatabase(@ApplicationContext appContext: Context): BaseProDB {
        return Room.databaseBuilder(
            appContext,
            BaseProDB::class.java,
            BaseProDB.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideBaseProDao(BaseProDB: BaseProDB): BaseProDao {
        return BaseProDB.baseproDao
    }

    @Provides
    @Singleton
    fun provideBaseProRepository(BaseProDao: BaseProDao): BaseProRepo {
        return BaseProRepoImpl(BaseProDao)
    }


    @BikeDatabase
    @Provides
    @Singleton
    fun provideBikeDatabase(@ApplicationContext context: Context): BaseProDB {
        return Room.databaseBuilder(
            context,
            BaseProDB::class.java,
            "bike_database.db"
        ).build()
    }

    @OtherDatabase
    @Provides
    @Singleton
    fun provideOtherDatabase(@ApplicationContext context: Context): BaseProDB {
        return Room.databaseBuilder(
            context,
            BaseProDB::class.java,
            "list_database.db"
        ).build()
    }

}