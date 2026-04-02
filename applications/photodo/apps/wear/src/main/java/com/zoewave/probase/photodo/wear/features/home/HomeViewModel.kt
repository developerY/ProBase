package com.zoewave.probase.photodo.wear.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.photodo.data.SyncDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncDataStore: SyncDataStore
) : ViewModel() {

    private val TAG = "PhotoDoHomeViewModel"

    val uiState: StateFlow<HomeUiState> = syncDataStore.latestSyncDataFlow
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
                    progressPercentage = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
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
}
