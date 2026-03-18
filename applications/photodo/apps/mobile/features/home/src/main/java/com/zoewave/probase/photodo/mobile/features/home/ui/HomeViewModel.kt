package com.zoewave.probase.photodo.mobile.features.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo
    // Inject your repositories here later (e.g., private val tasksRepo: TasksRepository)
) : ViewModel() {
    val TAG = "HomeViewModel"

    /*private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()*/

    // 1. Directly map the relational database stream into our UI State
    val uiState: StateFlow<HomeUiState> = photoDoRepo.getCategoriesWithTaskLists()
        .map { categoriesWithLists ->
            if (categoriesWithLists.isEmpty()) return@map HomeUiState.Empty

            // 3. Map the DB entities to our UI models and calculate the math
            val overviewModels = categoriesWithLists.map { groupedData ->

                val category = groupedData.category
                val taskLists = groupedData.taskLists

                val totalTasks = taskLists.size

                // Based on your schema, we count how many lists are marked "Completed"
                val completedTasks = taskLists.count { it.status == "Completed" }

                // Protect against divide-by-zero!
                val progressPercentage = if (totalTasks > 0) {
                    completedTasks.toFloat() / totalTasks.toFloat()
                } else {
                    0f
                }

                CategoryOverviewUiModel(
                    id = category.categoryId,
                    name = category.name,
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    progressPercentage = progressPercentage
                )
            }

            // 4. Return the populated success state
            HomeUiState.Success(overviewModels)
        }
        .catch { e ->
            Log.e(TAG, "Error calculating home overview stats", e)
            // If something goes wrong, we could emit an Error state,
            // but falling back to Empty is often safer for dashboards.
            emit(HomeUiState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000), // Pauses math when app is in background!
            initialValue = HomeUiState.Loading
        )

    /*init {
        loadDashboardData()
    }*/

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnRefresh -> loadDashboardData()
            is HomeEvent.OnTaskClicked -> {
                // Handle navigation or detail expansion logic here
            }
            is HomeEvent.OnTaskToggled -> toggleTask(event.taskId, event.isCompleted)
            is HomeEvent.OnCategoryClicked -> {
                // With Nav3, navigation is usually intercepted directly in the HomeUiRoute.
                // We just log it here for debugging purposes!
                Log.d(TAG, "Category clicked: ${event.categoryName} (ID: ${event.categoryId})")
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            //_uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Simulate network/database delay
            delay(1000)

            /*_uiState.update { state ->
                state.copy(
                    isLoading = false,
                    recentPhotoTasks = listOf(
                        PhotoTask("1", "Take photo of the sunset", false),
                        PhotoTask("2", "Scan receipt for expenses", true),
                        PhotoTask("3", "Organize holiday album", false)
                    )
                )
            }*/
        }
    }

    private fun toggleTask(taskId: String, isCompleted: Boolean) {
        /*_uiState.update { state ->
            val updatedTasks = state.recentPhotoTasks.map { task ->
                if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
            }
            state.copy(recentPhotoTasks = updatedTasks)
        }*/
    }
}