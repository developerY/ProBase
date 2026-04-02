package com.zoewave.probase.applications.photodo.db.sync

import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import javax.inject.Inject

/**
 * A no-op implementation of [TaskSyncEngine] used when no real sync engine is provided.
 */
class NoOpTaskSyncEngine @Inject constructor() : TaskSyncEngine {
    override suspend fun syncTaskUpdate(task: TaskEntity) {
        // Do nothing
    }
}
