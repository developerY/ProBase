package com.zoewave.probase.photodo.data.di

import com.zoewave.probase.applications.photodo.db.sync.NoOpTaskSyncEngine
import com.zoewave.probase.applications.photodo.db.sync.TaskSyncEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    /**
     * Bind [NoOpTaskSyncEngine] to [TaskSyncEngine] for the Phone-to-Watch one-way sync.
     * The [com.zoewave.probase.photodo.data.PhotoDoSyncEngine] handles broadcasting state separately
     * by observing repository flows, which avoids circular dependencies between the repo and sync engine.
     */
    @Binds
    @Singleton
    abstract fun bindTaskSyncEngine(
        noOpTaskSyncEngine: NoOpTaskSyncEngine
    ): TaskSyncEngine
}
