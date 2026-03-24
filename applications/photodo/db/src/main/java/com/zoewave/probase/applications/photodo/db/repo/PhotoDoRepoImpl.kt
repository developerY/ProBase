package com.zoewave.probase.applications.photodo.db.repo

import android.util.Log
import com.zoewave.probase.applications.photodo.db.PhotoDoDao
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithTaskLists
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListWithPhotos
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PhotoDoRepoImpl @Inject constructor(
    private val photoDoDao: PhotoDoDao
) : PhotoDoRepo {

    // --- Category Operations ---
    override suspend fun insertCategory(category: CategoryEntity): Long {
        return photoDoDao.insertCategory(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        photoDoDao.deleteCategory(category)
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return photoDoDao.getAllCategories()
    }

    override fun getCategoryById(categoryId: Long): Flow<CategoryEntity?> {
        return photoDoDao.getCategoryById(categoryId)
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        photoDoDao.updateCategory(category) // ✅ Uncommented for safety
    }

    override fun getCategoriesWithTaskLists(): Flow<List<CategoryWithTaskLists>> {
        return photoDoDao.getCategoriesWithTaskLists()
    }

    // --- TaskList Operations ---
    override suspend fun insertTaskList(taskList: TaskListEntity) : Long {
        return photoDoDao.insertTaskList(taskList)
    }

    override suspend fun deleteTaskList(taskList: TaskListEntity) {
        photoDoDao.deleteTaskList(taskList)
    }

    override suspend fun deleteTaskListById(listId: Long) {
        photoDoDao.deleteTaskListById(listId)
    }

    override fun getTaskListById(listId: Long): Flow<TaskListEntity?> {
        return photoDoDao.getTaskListById(listId)
    }

    override fun getTaskListsForCategory(categoryId: Long): Flow<List<TaskListEntity>> {
        return photoDoDao.getTaskListsForCategory(categoryId)
    }

    // ✅ ADDED: The concrete implementations pointing to the DAO
    override suspend fun updateProjectUrgency(listId: Long, isUrgent: Boolean) {
        photoDoDao.updateProjectUrgency(listId, isUrgent)
    }

    override suspend fun updateProjectFavorite(listId: Long, isFavorite: Boolean) {
        photoDoDao.updateProjectFavorite(listId, isFavorite)
    }

    // --- Task Item Operations ---
    override suspend fun insertTaskItem(item: TaskItemEntity) {
        photoDoDao.insertTaskItem(item)
    }

    override suspend fun updateTaskItem(item: TaskItemEntity) {
        photoDoDao.updateTaskItem(item)
    }

    override suspend fun deleteTaskItem(item: TaskItemEntity) {
        photoDoDao.deleteTaskItem(item)
    }

    // --- Photo Operations ---
    override suspend fun insertPhoto(photo: PhotoEntity) {
        photoDoDao.insertPhoto(photo)
    }

    override suspend fun deletePhoto(photo: PhotoEntity) {
        photoDoDao.deletePhoto(photo)
    }

    override fun getPhotosForTaskList(listId: Long): Flow<List<PhotoEntity>> {
        return photoDoDao.getPhotosForTaskList(listId)
    }

    // --- Relational Operations ---
    override fun getTaskListWithPhotos(listId: Long): Flow<TaskListWithPhotos?> {
        return photoDoDao.getTaskListWithPhotos(listId)
    }

    override suspend fun clearAllData() {
        Log.d("PhotoDoRepoImpl", "Cleared all data")
        photoDoDao.clearPhotos()
        photoDoDao.clearTaskItems()
        photoDoDao.clearTaskLists()
        photoDoDao.clearCategories()
    }
}