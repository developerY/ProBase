package com.zoewave.probase.di

import com.zoewave.probase.applications.photodo.db.sync.NoOpTaskSyncEngine
import com.zoewave.probase.applications.photodo.db.sync.TaskSyncEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PhotoDoAppBridgeModule {

    @Binds
    @Singleton
    abstract fun bindTaskSyncEngine(
        impl: NoOpTaskSyncEngine
    ): TaskSyncEngine
}
