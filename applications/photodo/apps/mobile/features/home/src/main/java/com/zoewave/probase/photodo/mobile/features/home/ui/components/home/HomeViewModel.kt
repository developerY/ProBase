package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
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
    val uiState: StateFlow<HomeUiState> = photoDoRepo.getCategoriesWithProjectsAndTasks()
        .map { categoriesWithProjectsAndTasks ->
            if (categoriesWithProjectsAndTasks.isEmpty()) return@map HomeUiState.Empty

            val overviewModels = ArrayList<CategoryOverviewUiModel>(categoriesWithProjectsAndTasks.size)
            val urgentProjects = ArrayList<ProjectListUiModel>()

            for (groupedData in categoriesWithProjectsAndTasks) {
                val category = groupedData.category
                val projectsWithTasks = groupedData.projects

                var totalTasksInCategory = 0
                var completedTasksInCategory = 0

                for (projectWithTasks in projectsWithTasks) {
                    val project = projectWithTasks.project
                    val tasks = projectWithTasks.tasks

                    totalTasksInCategory += tasks.size
                    completedTasksInCategory += tasks.count { it.isChecked }

                    if (project.isFavorite || project.isUrgent) {
                        urgentProjects.add(
                            ProjectListUiModel(
                                projectId = project.projectId,
                                title = project.name,
                                categoryName = category.name,
                                isFavorite = project.isFavorite,
                                isUrgent = project.isUrgent,
                                currentSpend = project.currentSpend,
                                projectBudget = project.projectBudget,
                                dueDateMillis = project.dueDate
                            )
                        )
                    }
                }

                val progressPercentage = if (totalTasksInCategory > 0) {
                    completedTasksInCategory.toFloat() / totalTasksInCategory.toFloat()
                } else {
                    0f
                }

                overviewModels.add(
                    CategoryOverviewUiModel(
                        id = category.categoryId,
                        name = category.name,
                        totalTasks = totalTasksInCategory,
                        completedTasks = completedTasksInCategory,
                        progressPercentage = progressPercentage
                    )
                )
            }

            HomeUiState.Success(
                categories = overviewModels,
                urgentProjects = urgentProjects
            )
        }
        .flowOn(Dispatchers.Default)
        .catch { e ->
            Log.e(TAG, "Error calculating home overview stats", e)
            // If something goes wrong, we could emit an Error state,
            // but falling back to Empty is often safer for dashboards.
            emit(HomeUiState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    /*init {
        loadDashboardData()
    }*/

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnRefresh -> {}
            is HomeEvent.OnTaskClicked -> {}
            is HomeEvent.OnTaskToggled -> {}
            is HomeEvent.OnCategoryClicked -> {
                // With Nav3, navigation is usually intercepted directly in the HomeUiRoute.
                // We just log it here for debugging purposes!
                Log.d(TAG, "Category clicked: ${event.categoryName} (ID: ${event.categoryId})")
            }

            is HomeEvent.OnAddCategory -> {
                viewModelScope.launch {
                    try {
                        val newCategory = CategoryEntity(
                            name = event.name,
                            description = event.description ?: "Created from Dashboard"
                        )
                        // Updated to use the modern upsert API from the repository
                        photoDoRepo.upsertCategory(newCategory)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving new category", e)
                    }
                }
            }

            is HomeEvent.OnCreateFromTemplate -> {
                viewModelScope.launch {
                    try {
                        val categoryId = photoDoRepo.getOrCreateCategoryByName(event.template.categoryName)
                        val newProject = ProjectEntity(
                            categoryId = categoryId,
                            name = event.template.title,
                            projectBudget = event.template.defaultBudget,
                            notes = "Created from ${event.template.title} template"
                        )
                        photoDoRepo.upsertProject(newProject)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating project from template", e)
                    }
                }
            }

            is HomeEvent.OnDeleteCategory -> {
                viewModelScope.launch {
                    try {
                        val category = photoDoRepo.getCategoryById(event.categoryId).first()
                        if (category != null) {
                            photoDoRepo.deleteCategory(category)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting category", e)
                    }
                }
            }
        }
    }
}