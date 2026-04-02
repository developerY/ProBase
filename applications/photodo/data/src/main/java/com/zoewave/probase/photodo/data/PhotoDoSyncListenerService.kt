package com.zoewave.probase.photodo.data

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PhotoDoSync"

@AndroidEntryPoint
class PhotoDoSyncListenerService : WearableListenerService() {

    @Inject
    lateinit var repo: PhotoDoRepo

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path != null && path.startsWith("/photodo/task_update/")) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val dataMap = dataMapItem.dataMap
                    
                    val syncId = dataMap.getString("syncId")
                    val isChecked = dataMap.getBoolean("isChecked")
                    val lastModified = dataMap.getLong("lastModified")

                    if (syncId != null) {
                        serviceScope.launch {
                            try {
                                Log.d(TAG, "Received sync update for task $syncId: isChecked=$isChecked")
                                
                                // Check if we already have a newer version locally
                                val localTask = repo.getTaskBySyncId(syncId)
                                if (localTask == null || localTask.lastModified < lastModified) {
                                    repo.updateTaskStatusBySyncId(syncId, isChecked, lastModified)
                                    Log.i(TAG, "Successfully updated task status from sync: $syncId")
                                } else {
                                    Log.d(TAG, "Ignoring sync update for task $syncId: local version is newer or same")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to update task status from sync", e)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
