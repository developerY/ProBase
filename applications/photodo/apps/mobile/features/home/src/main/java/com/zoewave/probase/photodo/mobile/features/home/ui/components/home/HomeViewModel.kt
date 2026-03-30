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
    val uiState: StateFlow<HomeUiState> = photoDoRepo.getCategoriesWithProjects()
        .map { categoriesWithLists ->
            if (categoriesWithLists.isEmpty()) return@map HomeUiState.Empty

            val overviewModels = mutableListOf<CategoryOverviewUiModel>()
            val urgentProjects = mutableListOf<ProjectListUiModel>() // ✅ Temporary list

            categoriesWithLists.forEach { groupedData ->
                val category = groupedData.category
                val projects = groupedData.projects

                // --- YOUR EXISTING MATH LOGIC ---
                val totalTasks = projects.size

                // Based on your schema, we count how many lists are marked "Completed"
                val completedTasks = projects.count { it.status == "Completed" }

                // Protect against divide-by-zero!
                val progressPercentage = if (totalTasks > 0) {
                    completedTasks.toFloat() / totalTasks.toFloat()
                } else {
                    0f
                }

                overviewModels.add(
                    CategoryOverviewUiModel(
                        id = category.categoryId,
                        name = category.name,
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        progressPercentage = progressPercentage
                    )
                )

                // --- ✅ NEW: FILTER URGENT PROJECTS AT THE SAME TIME ---
                projects.filter { it.isFavorite || it.isUrgent }.forEach { project ->
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

            // Return the populated success state with BOTH lists!
            HomeUiState.Success(
                categories = overviewModels,
                urgentProjects = urgentProjects
            )
        }
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
                        // Assuming you have an insertCategory function in your Repo!
                        photoDoRepo.insertCategory(newCategory)
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
                        photoDoRepo.insertProject(newProject)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating project from template", e)
                    }
                }
            }
        }
    }
}