package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.home.ui.components.categories.CategoryOverviewUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {
    val TAG = "HomeViewModel"

    /*private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()*/

    private data class UiFlags(
        val isQuickProjectSheetOpen: Boolean = false,
        val quickProjectCategoryOverride: String? = null,
        val categorySearchQuery: String = "",
        val taskSearchQuery: String = "",
        val isCategoriesSummaryExpanded: Boolean = true,
        val showAddCategoryDialog: Boolean = false,
        val categoryToDelete: CategoryOverviewUiModel? = null,
        val fabMenuExpanded: Boolean = false,
        val isSearchMode: Boolean = false,
        val searchScope: SearchScope = SearchScope.CATEGORIES
    )

    private val _uiFlags = MutableStateFlow(UiFlags())

    // 1. Directly map the relational database stream into our UI State
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        photoDoRepo.getCategoriesWithProjectsAndTasks(),
        _uiFlags.flatMapLatest { flags ->
            val query = if (flags.isSearchMode) flags.categorySearchQuery else flags.taskSearchQuery
            if (query.length >= 2) {
                // Combine results from searching project names and matching tasks
                combine(
                    photoDoRepo.searchProjectsWithDetails(query),
                    photoDoRepo.getProjectsWithMatchingTasks(query)
                ) { byName, byTask ->
                    // Merge and deduplicate by projectId
                    val merged = (byName + byTask).distinctBy { it.project.projectId }
                    merged.map { projectDetails ->
                        TaskSearchResult(
                            projectId = projectDetails.project.projectId,
                            projectTitle = projectDetails.project.name,
                            tasks = projectDetails.tasks.filter { 
                                it.text.contains(query, ignoreCase = true) 
                            }
                        )
                    }
                }
            } else {
                flowOf(emptyList<TaskSearchResult>())
            }
        },
        _uiFlags,
        appSettingsRepository.isAiEnabledFlow,
        appSettingsRepository.animationsEnabledFlow
    ) { categoriesWithProjectsAndTasks, searchResults, flags, isAiEnabled, animationsEnabled ->
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
                                dueDateMillis = project.dueDate,
                                doneTasksCount = tasks.count { it.isChecked },
                                totalTasksCount = tasks.size,
                                thumbnailUri = projectWithTasks.photos.firstOrNull()?.photoUri
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
                        totalProjects = projectsWithTasks.size,
                        totalTasks = totalTasksInCategory,
                        completedTasks = completedTasksInCategory,
                        progressPercentage = progressPercentage
                    )
                )
            }

            HomeUiState(
                categories = overviewModels,
                urgentProjects = urgentProjects,
                isQuickProjectSheetOpen = flags.isQuickProjectSheetOpen,
                quickProjectCategoryOverride = flags.quickProjectCategoryOverride,
                categorySearchQuery = flags.categorySearchQuery,
                taskSearchQuery = flags.taskSearchQuery,
                taskSearchResults = searchResults,
                isAiEnabled = isAiEnabled,
                isCategoriesSummaryExpanded = flags.isCategoriesSummaryExpanded,
                showAddCategoryDialog = flags.showAddCategoryDialog,
                categoryToDelete = flags.categoryToDelete,
                fabMenuExpanded = flags.fabMenuExpanded,
                isSearchMode = flags.isSearchMode,
                searchScope = flags.searchScope,
                animationsEnabled = animationsEnabled
            )
        }
        .flowOn(Dispatchers.Default)
        .catch { e ->
            Log.e(TAG, "Error calculating home overview stats", e)
            // If something goes wrong, we could emit an Error state,
            // but falling back to Empty is often safer for dashboards.
            emit(HomeUiState())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
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

            is HomeEvent.OnAddQuickProjectClicked -> {
                _uiFlags.update { 
                    it.copy(
                        isQuickProjectSheetOpen = true,
                        quickProjectCategoryOverride = event.overrideCategoryName
                    ) 
                }
            }

            is HomeEvent.OnDismissBottomSheet -> {
                _uiFlags.update { 
                    it.copy(
                        isQuickProjectSheetOpen = false,
                        quickProjectCategoryOverride = null
                    ) 
                }
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

            is HomeEvent.OnCategorySearchQueryChanged -> {
                _uiFlags.update { 
                    it.copy(categorySearchQuery = event.query)
                }
            }

            is HomeEvent.OnTaskSearchQueryChanged -> {
                _uiFlags.update { 
                    it.copy(taskSearchQuery = event.query)
                }
            }

            is HomeEvent.OnToggleCategoriesSummary -> {
                _uiFlags.update { 
                    it.copy(isCategoriesSummaryExpanded = !it.isCategoriesSummaryExpanded)
                }
            }

            is HomeEvent.OnShowAddCategoryDialog -> {
                _uiFlags.update { it.copy(showAddCategoryDialog = event.show) }
            }

            is HomeEvent.OnCategoryToDeleteChanged -> {
                _uiFlags.update { it.copy(categoryToDelete = event.category) }
            }

            is HomeEvent.OnFabMenuToggle -> {
                _uiFlags.update { it.copy(fabMenuExpanded = event.expanded) }
            }

            is HomeEvent.OnSearchModeToggle -> {
                _uiFlags.update { it.copy(isSearchMode = event.enabled) }
            }

            is HomeEvent.OnSearchScopeChanged -> {
                _uiFlags.update { it.copy(searchScope = event.scope) }
            }
        }
    }
}
