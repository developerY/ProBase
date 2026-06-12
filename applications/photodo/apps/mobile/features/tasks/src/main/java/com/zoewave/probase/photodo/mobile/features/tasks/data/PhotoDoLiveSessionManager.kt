package com.zoewave.probase.photodo.mobile.features.tasks.data

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ai.type.*
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.features.ai.firebase.domain.GeminiFirebaseManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PhotoDoLiveSession"

@OptIn(PublicPreviewAPI::class)
@Singleton
class PhotoDoLiveSessionManager @Inject constructor(
    private val geminiFirebaseManager: GeminiFirebaseManager,
    private val photoDoRepo: PhotoDoRepo
) : DefaultLifecycleObserver {

    private var session: LiveSession? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private var currentProjectId: Long? = null

    fun setProjectId(projectId: Long) {
        currentProjectId = projectId
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startConversation() {
        if (_isSessionActive.value) return

        scope.launch {
            try {
                // 1. Define tools for Gemini to interact with Photodo
                val tools = listOf(
                    Tool.functionDeclarations(
                        listOf(
                            FunctionDeclaration(
                                name = "addTask",
                                description = "Adds a new task to the current project checklist",
                                parameters = mapOf(
                                    "text" to Schema.string("The description of the task")
                                )
                            ),
                            FunctionDeclaration(
                                name = "setTaskStatus",
                                description = "Updates the completion status of a task",
                                parameters = mapOf(
                                    "taskId" to Schema.long("The ID of the task to update"),
                                    "isChecked" to Schema.boolean("True if completed, false otherwise")
                                )
                            )
                        )
                    )
                )

                val liveModel = geminiFirebaseManager.createLiveModel()
                session = liveModel.connect()
                
                _isSessionActive.value = true
                Log.d(TAG, "Connected to Gemini Live session for Photodo")
                
                // 2. Start the bidirectional audio conversation
                session?.startAudioConversation(
                    functionCallHandler = { toolCall ->
                        // Handle tool calls from Gemini
                        handleToolCall(toolCall)
                        FunctionResponsePart(
                            name = toolCall.name,
                            response = buildJsonObject { put("status", "success") },
                            id = toolCall.id
                        )
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start conversation: ${e.message}")
                _isSessionActive.value = false
            }
        }
    }

    private fun handleToolCall(toolCall: FunctionCallPart) {
        scope.launch {
            when (toolCall.name) {
                "addTask" -> {
                    val text = toolCall.args["text"]?.jsonPrimitive?.contentOrNull
                    if (text != null) handleAddTask(text)
                }
                "setTaskStatus" -> {
                    val taskId = toolCall.args["taskId"]?.jsonPrimitive?.longOrNull
                    val isChecked = toolCall.args["isChecked"]?.jsonPrimitive?.booleanOrNull
                    if (taskId != null && isChecked != null) handleSetTaskStatus(taskId, isChecked)
                }
            }
        }
    }

    fun stopConversation() {
        scope.launch {
            session?.close()
            session = null
            _isSessionActive.value = false
            Log.d(TAG, "Conversation stopped")
        }
    }

    private suspend fun handleAddTask(text: String) {
        val projectId = currentProjectId ?: return
        photoDoRepo.upsertTask(TaskEntity(projectId = projectId, text = text, isChecked = false))
        Log.d(TAG, "Tool: Task added - $text")
    }

    private suspend fun handleSetTaskStatus(taskId: Long, isChecked: Boolean) {
        val task = photoDoRepo.getTasksForProject(currentProjectId ?: return).first().find { it.taskId == taskId }
        if (task != null) {
            photoDoRepo.updateTask(task.copy(isChecked = isChecked))
            Log.d(TAG, "Tool: Task status updated - $taskId to $isChecked")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopConversation()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        scope.cancel()
    }
}
