package com.zoewave.probase.photodo.wear.features.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.zoewave.probase.photodo.data.SyncDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncDataStore: SyncDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "PhotoDoHomeViewModel"

    val uiState: StateFlow<HomeUiState> = syncDataStore.latestSyncDataFlow
        .onEach { categories ->
            if (categories.isEmpty()) {
                Log.d(TAG, "Data is empty on init, triggering proactive sync request")
                requestSync()
            }
        }
        .map { categories ->
            if (categories.isEmpty()) return@map HomeUiState.Empty

            val mappedCategories = categories.map { syncCategory ->
                var totalTasks = 0
                var completedTasks = 0

                syncCategory.projects.forEach { project ->
                    totalTasks += project.tasks.size
                    completedTasks += project.tasks.count { it.isCompleted }
                }

                CategoryWearUiModel(
                    id = syncCategory.id,
                    name = syncCategory.name,
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    progressPercentage = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f,
                    hasPhoto = syncCategory.hasPhoto
                )
            }

            HomeUiState.Success(categories = mappedCategories)
        }
        .catch { e ->
            Log.e(TAG, "Error loading categories from DataStore", e)
            emit(HomeUiState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnRequestSync -> requestSync()
            is HomeEvent.OnCategoryClick -> {
                // Handled in Route for navigation
            }
        }
    }

    private fun requestSync() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending request_sync message to phone...")
                val nodeClient = Wearable.getNodeClient(context)
                val messageClient = Wearable.getMessageClient(context)
                
                val nodes: List<Node> = nodeClient.connectedNodes.await()
                if (nodes.isEmpty()) {
                    Log.w(TAG, "No connected nodes found to send sync request")
                }
                
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, "/photodo/request_sync", null).await()
                    Log.d(TAG, "Sync request message sent to node: ${node.displayName}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send sync request message", e)
            }
        }
    }
}
