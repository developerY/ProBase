package com.zoewave.probase.photodo.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.sync.TaskSyncEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "PhotoDoSync"

class PhotoDoSyncEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : TaskSyncEngine {

    private val dataClient = Wearable.getDataClient(context)

    override suspend fun syncTaskUpdate(task: TaskEntity) {
        try {
            Log.d(TAG, "Syncing task update: ${task.text} (checked: ${task.isChecked})")

            // Create a unique path for this task's sync item
            val request = PutDataMapRequest.create("/photodo/task_update/${task.globalSyncId}")
            
            request.dataMap.apply {
                putString("syncId", task.globalSyncId)
                putBoolean("isChecked", task.isChecked)
                putLong("lastModified", task.lastModified)
                // We add a timestamp to ensure the Data Layer treats this as an update 
                // even if the boolean state is the same (though usually it won't be)
                putLong("eventTime", System.currentTimeMillis())
            }

            val putDataReq = request.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataReq).await()
            
            Log.d(TAG, "Task sync data item successfully put: ${task.globalSyncId}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync task update", e)
        }
    }
}
