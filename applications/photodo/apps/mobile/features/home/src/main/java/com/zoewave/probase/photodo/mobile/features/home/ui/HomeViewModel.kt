package com.zoewave.probase.photodo.mobile.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.photodo.model.PhotoTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // Inject your repositories here later (e.g., private val tasksRepo: TasksRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnRefresh -> loadDashboardData()
            is HomeEvent.OnTaskClicked -> {
                // Handle navigation or detail expansion logic here
            }
            is HomeEvent.OnTaskToggled -> toggleTask(event.taskId, event.isCompleted)
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Simulate network/database delay
            delay(1000)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    recentPhotoTasks = listOf(
                        PhotoTask("1", "Take photo of the sunset", false),
                        PhotoTask("2", "Scan receipt for expenses", true),
                        PhotoTask("3", "Organize holiday album", false)
                    )
                )
            }
        }
    }

    private fun toggleTask(taskId: String, isCompleted: Boolean) {
        _uiState.update { state ->
            val updatedTasks = state.recentPhotoTasks.map { task ->
                if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
            }
            state.copy(recentPhotoTasks = updatedTasks)
        }
    }
}