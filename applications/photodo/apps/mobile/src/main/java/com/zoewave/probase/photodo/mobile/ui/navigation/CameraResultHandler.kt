package com.zoewave.probase.photodo.mobile.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.photodo.mobile.features.tasks.domain.AddPhotoToTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraResultHandler @Inject constructor(
    private val addPhotoToTask: AddPhotoToTaskUseCase // ✅ Inject the pure UseCase
) : ViewModel() {

    fun execute(projectId: Long, uri: String) {
        // We just use the ViewModel to get access to this scope!
        viewModelScope.launch {
            addPhotoToTask(projectId, uri)
        }
    }
}