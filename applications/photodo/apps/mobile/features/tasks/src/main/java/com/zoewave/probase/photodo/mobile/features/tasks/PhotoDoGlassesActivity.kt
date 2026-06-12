package com.zoewave.probase.photodo.mobile.features.tasks

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.xr.glimmer.GlimmerTheme
import com.zoewave.probase.photodo.mobile.features.tasks.data.PhotoDoLiveSessionManager
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.ProjectedTaskDetailScreen
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailEvent
import com.zoewave.probase.photodo.mobile.features.tasks.ui.detail.TaskDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PhotoDoGlassesActivity : ComponentActivity() {

    @Inject
    lateinit var liveSessionManager: PhotoDoLiveSessionManager
    
    private val viewModel: TaskDetailViewModel by viewModels()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectId = intent.getLongExtra("projectId", -1L)
        if (projectId != -1L) {
            viewModel.loadTaskDetails(projectId)
            liveSessionManager.setProjectId(projectId)
            lifecycle.addObserver(liveSessionManager)
            
            // Auto-start Gemini Live when the projected activity begins
            // (Phone app is responsible for RECORD_AUDIO permission)
            liveSessionManager.startConversation()
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val isAiActive by liveSessionManager.isSessionActive.collectAsState()

            GlimmerTheme {
                ProjectedTaskDetailScreen(
                    uiState = uiState,
                    isAiActive = isAiActive,
                    aiAudioLevel = { 0f }, // Linked to session manager in production
                    onToggleTask = { taskId, isChecked ->
                        // In projection, we find the task entity and fire the event
                        // (Simplified for demo)
                    }
                )
            }
        }
    }
}
