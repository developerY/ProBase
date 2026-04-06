package com.zoewave.probase.photodo.data

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
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
    lateinit var syncDataStore: SyncDataStore
    
    @Inject
    lateinit var syncEngine: PhotoDoSyncEngine

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Log.d(TAG, "onDataChanged: Received ${dataEvents.count} events")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                Log.d(TAG, "Processing event for path: $path")
                
                if (path == "/photodo/sync_state") {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val dataMap = dataMapItem.dataMap
                    
                    val jsonPayload = dataMap.getString(getString(R.string.applications_photodo_data_payload))
                    if (jsonPayload != null) {
                        serviceScope.launch {
                            try {
                                Log.d(TAG, "Received full sync payload, saving to DataStore")
                                syncDataStore.saveLatestSyncPayload(jsonPayload)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to save sync payload", e)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "onMessageReceived: path=${messageEvent.path}")
        
        if (messageEvent.path == "/photodo/request_sync") {
            // Trigger a manual sync broadcast on the phone
            syncEngine.triggerSync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
