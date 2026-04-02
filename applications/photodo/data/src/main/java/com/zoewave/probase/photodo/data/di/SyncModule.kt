package com.zoewave.probase.photodo.data.di

import com.zoewave.probase.photodo.data.PhotoDoSyncEngine
import com.zoewave.probase.applications.photodo.db.sync.TaskSyncEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindTaskSyncEngine(
        photoDoSyncEngine: PhotoDoSyncEngine
    ): TaskSyncEngine
}
