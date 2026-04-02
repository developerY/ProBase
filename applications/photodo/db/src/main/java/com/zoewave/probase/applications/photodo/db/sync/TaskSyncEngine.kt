package com.zoewave.probase.applications.photodo.db.sync

import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

/**
 * Interface for synchronizing checklist task status across devices via the Wearable Data Layer.
 */
interface TaskSyncEngine {
    /**
     * Pushes a task update to other connected devices.
     */
    suspend fun syncTaskUpdate(task: TaskEntity)
}
