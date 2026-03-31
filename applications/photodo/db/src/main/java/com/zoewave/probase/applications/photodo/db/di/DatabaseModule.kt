package com.zoewave.probase.applications.photodo.db.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.zoewave.probase.applications.photodo.db.PhotoDoDB
import com.zoewave.probase.applications.photodo.db.PhotoDoDao
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepoImpl
import com.zoewave.probase.applications.photodo.db.seed.PhotoDoOnboardingData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePhotoDoDatabase(
        @ApplicationContext context: Context,
        callback: PhotoDoDatabaseCallback // 🚀 Hilt will automatically construct and inject this
    ): PhotoDoDB {
        return Room.databaseBuilder(
            context,
            PhotoDoDB::class.java,
            "photodo_database"
        )
            .setDriver(BundledSQLiteDriver())
            // Drops only Room's tables when the schema changes during development.
            .fallbackToDestructiveMigration(false) // Handles schema changes during dev
            .addCallback(callback)
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

// 🚀 Pulled out into its own class.
// We inject Provider<PhotoDoDB> to prevent circular dependency crashes.
class PhotoDoDatabaseCallback @Inject constructor(
    private val databaseProvider: Provider<PhotoDoDB>
) : RoomDatabase.Callback() {

    // Create a dedicated scope for database prepopulation
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun onCreate(connection: SQLiteConnection) {
        super.onCreate(connection)

        // Fire off a background coroutine so the UI doesn't stutter on first launch
        applicationScope.launch {
            populateDatabase()
        }
    }

    private suspend fun populateDatabase() {
        // Safely extract the DAO from the lazily loaded database provider
        val dao = databaseProvider.get().photoDoDao()

        // Bulk upsert the onboarding data for better performance and safety
        dao.upsertCategories(PhotoDoOnboardingData.defaultCategories)
        dao.upsertProjects(PhotoDoOnboardingData.defaultProjects)
        dao.upsertTasks(PhotoDoOnboardingData.defaultTasks)
    }
}
