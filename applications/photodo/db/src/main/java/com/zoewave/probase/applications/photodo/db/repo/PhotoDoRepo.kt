package com.zoewave.probase.applications.photodo.db.repo

import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithTaskLists
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListWithPhotos
import kotlinx.coroutines.flow.Flow

interface PhotoDoRepo {

    // --- Category Operations ---
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun deleteCategory(category: CategoryEntity)
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>
    suspend fun updateCategory(category: CategoryEntity)
    fun getCategoriesWithTaskLists(): Flow<List<CategoryWithTaskLists>>

    // --- TaskList Operations ---
    suspend fun insertTaskList(taskList: TaskListEntity): Long
    suspend fun deleteTaskList(taskList: TaskListEntity)
    suspend fun deleteTaskListById(listId: Long)
    fun getTaskListById(listId: Long): Flow<TaskListEntity?>
    fun getTaskListsForCategory(categoryId: Long): Flow<List<TaskListEntity>>

    suspend fun updateProjectUrgency(listId: Long, isUrgent: Boolean)
    suspend fun updateProjectFavorite(listId: Long, isFavorite: Boolean)

    // --- Task Item Operations (Checklist) ---
    suspend fun insertTaskItem(item: TaskItemEntity)
    suspend fun updateTaskItem(item: TaskItemEntity)
    suspend fun deleteTaskItem(item: TaskItemEntity)

    // --- Photo Operations ---
    suspend fun insertPhoto(photo: PhotoEntity)
    suspend fun deletePhoto(photo: PhotoEntity)
    fun getPhotosForTaskList(listId: Long): Flow<List<PhotoEntity>>

    // --- Relational Operations ---
    fun getTaskListWithPhotos(listId: Long): Flow<TaskListWithPhotos?>
    suspend fun clearAllData()
}