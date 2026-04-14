package com.zoewave.probase.photodo.features.smartadvice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.features.smartadvice.data.SmartAdviceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartAdviceViewModel @Inject constructor(
    private val repo: PhotoDoRepo,
    private val appSettings: AppSettingsRepository,
    private val engine: SmartAdviceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<SmartAdviceUiState>(SmartAdviceUiState.Idle)
    val uiState: StateFlow<SmartAdviceUiState> = _uiState.asStateFlow()

    fun fetchAdvice(projectId: Long) {
        viewModelScope.launch {
            _uiState.value = SmartAdviceUiState.Loading
            
            try {
                val projectDetails = repo.getProjectDetails(projectId).firstOrNull()
                val categoryId = projectDetails?.project?.categoryId
                val category = categoryId?.let { repo.getCategoryById(it).firstOrNull() }
                val apiKey = appSettings.getGeminiApiKey()
                val model = appSettings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"

                if (projectDetails != null && apiKey != null) {
                    val advice = engine.getAdvice(
                        project = projectDetails,
                        categoryName = category?.name ?: "Unknown",
                        apiKey = apiKey,
                        modelName = model
                    )
                    _uiState.value = SmartAdviceUiState.Success(advice)
                } else {
                    _uiState.value = SmartAdviceUiState.Error("Missing project data or API key.")
                }
            } catch (e: Exception) {
                _uiState.value = SmartAdviceUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
