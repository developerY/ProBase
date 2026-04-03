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
import kotlinx.coroutines.withContext
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

    private suspend fun broadcast(syncData: List<SyncCategory>) = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = Json.encodeToString(syncData)

            val request = PutDataMapRequest.create("/photodo/sync_state").apply {
                dataMap.putString("payload", jsonPayload)
                dataMap.putLong("timestamp", System.currentTimeMillis())
                
                // --- ATTACH PHOTO ASSETS ---
                // We need the project details again to get the actual URIs
                val allDetails = photoDoRepo.getAllProjectDetails().first()
                syncData.forEach { category ->
                    // 1. Attach Category Photo
                    if (category.hasPhoto) {
                        try {
                            val dbCategory = photoDoRepo.getCategoryById(category.id).first()
                            val categoryPhotoUri = dbCategory?.imageUri
                            if (categoryPhotoUri != null) {
                                // Decouple decoding and transformation to Default dispatcher
                                val bitmap = withContext(Dispatchers.Default) {
                                    loadBitmapFromUri(categoryPhotoUri)
                                }
                                if (bitmap != null) {
                                    val asset = withContext(Dispatchers.Default) {
                                        bitmap.toTinyGrayscaleAsset()
                                    }
                                    dataMap.putAsset("category_${category.id}", asset)
                                    Log.d(TAG, "Attached category asset: category_${category.id}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to attach category asset ${category.id}", e)
                        }
                    }

                    // 2. Attach Project Photos (Sync up to 5 thumbnails per project)
                    category.projects.forEach { project ->
                        if (project.hasPhoto) {
                            val details = allDetails.find { it.project.projectId == project.id }
                            val projectPhotos = details?.photos?.take(5) ?: emptyList()
                            
                            projectPhotos.forEachIndexed { index, photoEntity ->
                                try {
                                    val bitmap = withContext(Dispatchers.Default) {
                                        loadBitmapFromUri(photoEntity.photoUri)
                                    }
                                    if (bitmap != null) {
                                        val asset = withContext(Dispatchers.Default) {
                                            bitmap.toTinyGrayscaleAsset()
                                        }
                                        val assetKey = "photo_${project.id}_$index"
                                        dataMap.putAsset(assetKey, asset)
                                        Log.d(TAG, "Attached project asset: $assetKey")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to attach project asset ${project.id} index $index", e)
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
                hasPhoto = categoryWithProjects.category.imageUri != null,
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
