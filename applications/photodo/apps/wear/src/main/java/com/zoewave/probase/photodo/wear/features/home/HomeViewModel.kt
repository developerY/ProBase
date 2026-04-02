package com.zoewave.probase.photodo.wear.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo
) : ViewModel() {

    private val TAG = "PhotoDoHomeViewModel"

    val uiState: StateFlow<HomeUiState> = photoDoRepo.getCategoriesWithProjectsAndTasks()
        .map { data ->
            if (data.isEmpty()) return@map HomeUiState.Empty

            val categories = data.map { grouped ->
                val category = grouped.category
                val projects = grouped.projects

                var totalTasks = 0
                var completedTasks = 0

                projects.forEach { projectWithTasks ->
                    totalTasks += projectWithTasks.tasks.size
                    completedTasks += projectWithTasks.tasks.count { it.isChecked }
                }

                CategoryWearUiModel(
                    id = category.categoryId,
                    name = category.name,
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    progressPercentage = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
                )
            }

            HomeUiState.Success(categories = categories)
        }
        .catch { e ->
            Log.e(TAG, "Error loading categories", e)
            emit(HomeUiState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )
}
