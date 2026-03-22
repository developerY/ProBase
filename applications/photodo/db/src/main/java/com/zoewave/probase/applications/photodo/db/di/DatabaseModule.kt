package com.zoewave.probase.applications.photodo.db.di

import android.content.Context
import androidx.room.Room
import com.zoewave.probase.applications.photodo.db.PhotoDoDB
import com.zoewave.probase.applications.photodo.db.PhotoDoDao
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepoImpl
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
    fun providePhotoDoDatabase(
        @ApplicationContext context: Context,
    ): PhotoDoDB {
        return Room.databaseBuilder(
            context,
            PhotoDoDB::class.java,
            "photodo_database"
        )
            .fallbackToDestructiveMigration(false) // Handles schema changes gracefully during dev
        //.addCallback(callback.get())
        .build()
    }

    @Provides
    @Singleton
    fun providePhotoDoDao(database: PhotoDoDB): PhotoDoDao {
        return database.photoDoDao()
    }

    @Provides
    @Singleton
    fun providePhotoDoRepository(photoDoDao: PhotoDoDao): PhotoDoRepo {
        return PhotoDoRepoImpl(photoDoDao)
    }
}
