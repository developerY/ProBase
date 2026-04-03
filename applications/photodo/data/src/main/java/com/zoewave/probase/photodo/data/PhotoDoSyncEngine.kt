package com.zoewave.probase.photodo.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.data.util.toTinyGrayscaleAsset
import com.zoewave.probase.photodo.model.sync.SyncCategory
import com.zoewave.probase.photodo.model.sync.SyncProject
import com.zoewave.probase.photodo.model.sync.SyncTask
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PhotoDoSync"

/**
 * Sync engine that observes the Phone's Room database and broadcasts the full state to Wear OS.
 * Decoupled from [com.zoewave.probase.applications.photodo.db.sync.TaskSyncEngine] to avoid dependency cycles.
 */
@Singleton
class PhotoDoSyncEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoDoRepo: PhotoDoRepo
) {

    private val dataClient = Wearable.getDataClient(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startSyncing() {
        scope.launch {
            Log.d(TAG, "Starting sync observer...")
            // Combine categories/projects/tasks with project details (for photo counts)
            combine(
                photoDoRepo.getCategoriesWithProjectsAndTasks(),
                photoDoRepo.getAllProjectDetails()
            ) { hierarchy, details ->
                mapToSyncModels(hierarchy, details)
            }.collectLatest { syncData ->
                broadcast(syncData)
            }
        }
    }

    /**
     * Manually triggers a one-shot sync broadcast of the current state.
     * Useful for responding to "request sync" pings from the watch.
     */
    fun triggerSync() {
        scope.launch {
            try {
                Log.d(TAG, "Manual sync trigger received. Performing one-shot broadcast.")
                val hierarchy = photoDoRepo.getCategoriesWithProjectsAndTasks().first()
                val details = photoDoRepo.getAllProjectDetails().first()
                val syncData = mapToSyncModels(hierarchy, details)
                broadcast(syncData)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to perform manual sync trigger", e)
            }
        }
    }

    private suspend fun broadcast(syncData: List<SyncCategory>) {
        try {
            val jsonPayload = Json.encodeToString(syncData)

            val request = PutDataMapRequest.create("/photodo/sync_state").apply {
                dataMap.putString("payload", jsonPayload)
                dataMap.putLong("timestamp", System.currentTimeMillis())
                
                // --- ATTACH PHOTO ASSETS ---
                // We need the project details again to get the actual URIs
                val allDetails = photoDoRepo.getAllProjectDetails().first()
                syncData.forEach { category ->
                    category.projects.forEach { project ->
                        if (project.hasPhoto) {
                            val details = allDetails.find { it.project.projectId == project.id }
                            val primaryPhotoUri = details?.photos?.firstOrNull()?.photoUri
                            if (primaryPhotoUri != null) {
                                try {
                                    val bitmap = loadBitmapFromUri(primaryPhotoUri)
                                    if (bitmap != null) {
                                        val asset = bitmap.toTinyGrayscaleAsset()
                                        dataMap.putAsset("photo_${project.id}", asset)
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to attach asset for project ${project.id}", e)
                                }
                            }
                        }
                    }
                }
            }.asPutDataRequest().setUrgent()

            dataClient.putDataItem(request).await()
            Log.d(TAG, "Successfully broadcasted sync state (${syncData.size} categories)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast sync state", e)
        }
    }

    private fun loadBitmapFromUri(uriString: String): android.graphics.Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from URI: $uriString", e)
            null
        }
    }

    private fun mapToSyncModels(
        hierarchy: List<com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjectsAndTasks>,
        allDetails: List<ProjectDetails>
    ): List<SyncCategory> {
        val photoCounts = allDetails.associate { it.project.projectId to it.photos.size }
        val hasPhotos = allDetails.associate { it.project.projectId to it.photos.isNotEmpty() }

        return hierarchy.map { categoryWithProjects ->
            SyncCategory(
                id = categoryWithProjects.category.categoryId,
                name = categoryWithProjects.category.name,
                projects = categoryWithProjects.projects.map { projectWithTasks ->
                    SyncProject(
                        id = projectWithTasks.project.projectId,
                        name = projectWithTasks.project.name,
                        totalBudget = projectWithTasks.project.projectBudget,
                        spentAmount = projectWithTasks.project.currentSpend,
                        tasks = projectWithTasks.tasks.map { task ->
                            SyncTask(
                                id = task.taskId,
                                title = task.text,
                                isCompleted = task.isChecked
                            )
                        },
                        photoCount = photoCounts[projectWithTasks.project.projectId] ?: 0,
                        hasPhoto = hasPhotos[projectWithTasks.project.projectId] ?: false
                    )
                }
            )
        }
    }
}
