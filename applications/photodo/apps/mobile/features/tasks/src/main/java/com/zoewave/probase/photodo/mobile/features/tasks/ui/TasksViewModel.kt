package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
// import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
// import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity

@HiltViewModel
class TasksViewModel @Inject constructor(
    // private val repo: PhotoDoRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        // TODO: Collect from your DB here
        // repo.getAllTasks().map { entities -> mapToUiModels(entities) }
        //     .onEach { uiState.update { state -> state.copy(tasks = it) } }
        //     .launchIn(viewModelScope)
    }

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnAddRandomTaskClicked -> insertRandomTask()
            is TasksEvent.OnTaskToggled -> updateTask(event.taskId, event.isCompleted)
        }
    }

    private fun insertRandomTask() {
        viewModelScope.launch {
            val randomId = (1..100000).random().toLong()
            val randomTitle = "Test Task: ${UUID.randomUUID().toString().take(6)}"

            // TODO: Call your Room insert function here
            // repo.insertTask(TaskItemEntity(id = randomId, title = randomTitle, ...))

            // Simulated UI update for testing before DB is fully wired
            _uiState.update { state ->
                val newTask = TaskItemUiModel(randomId, randomTitle, false)
                state.copy(tasks = state.tasks + newTask)
            }
        }
    }

    private fun updateTask(taskId: Long, isCompleted: Boolean) {
        // TODO: Call your Room update function here
        // repo.updateTaskStatus(taskId, isCompleted)
    }
}