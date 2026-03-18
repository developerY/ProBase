package com.zoewave.probase.photodo.mobile.features.tasks.ui



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryTasksViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo
) : ViewModel() {

    private val _categoryId = MutableStateFlow<Long?>(null)
    private var currentCategoryName: String = ""

    // Reactively query the database whenever the ID is set!
    val uiState: StateFlow<TasksUiState> = _categoryId
        .filterNotNull()
        .flatMapLatest { id ->
            // Use the exact query you wrote earlier!
            photoDoRepo.getTaskListsForCategory(id)
                .map { taskLists ->
                    val mappedProjects = taskLists.map { entity ->
                        ProjectListUiModel(
                            id = entity.listId,
                            title = entity.name, // or name, depending on your entity
                            categoryName = currentCategoryName
                        )
                    }
                    // Reuse the existing state model
                    TasksUiState(projectLists = mappedProjects)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksUiState()
        )

    fun loadCategory(id: Long, name: String) {
        if (_categoryId.value == id) return
        currentCategoryName = name
        _categoryId.value = id
    }

    fun onEvent(event: TasksEvent) {
        // Because we are reusing TasksListScreen, we catch its events here!
        when (event) {
            is TasksEvent.OnAddListClicked -> {
                // TODO: Show your "Add List" bottom sheet.
                // AMAZING UX BONUS: Because we know the _categoryId.value,
                // you can automatically assign new lists to this category!
            }
            // Catch other relevant events...
            else -> {}
        }
    }
}