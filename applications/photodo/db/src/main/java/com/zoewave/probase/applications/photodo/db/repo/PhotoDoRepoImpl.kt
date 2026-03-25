package com.zoewave.probase.applications.photodo.db.repo

import android.util.Log
import com.zoewave.probase.applications.photodo.db.PhotoDoDao
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjects
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectWithPhotos
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
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
        photoDoDao.updateCategory(category)
    }

    override fun getCategoriesWithProjects(): Flow<List<CategoryWithProjects>> {
        return photoDoDao.getCategoriesWithProjects()
    }

    // --- Project Operations ---
    override suspend fun insertProject(project: ProjectEntity) : Long {
        return photoDoDao.insertProject(project)
    }

    override suspend fun deleteProject(project: ProjectEntity) {
        photoDoDao.deleteProject(project)
    }

    override suspend fun deleteProjectById(projectId: Long) {
        photoDoDao.deleteProjectById(projectId)
    }

    override fun getProjectById(projectId: Long): Flow<ProjectEntity?> {
        return photoDoDao.getProjectById(projectId)
    }

    override fun getProjectsForCategory(categoryId: Long): Flow<List<ProjectEntity>> {
        return photoDoDao.getProjectsForCategory(categoryId)
    }

    override suspend fun updateProjectUrgency(projectId: Long, isUrgent: Boolean) {
        photoDoDao.updateProjectUrgency(projectId, isUrgent)
    }

    override suspend fun updateProjectFavorite(projectId: Long, isFavorite: Boolean) {
        photoDoDao.updateProjectFavorite(projectId, isFavorite)
    }

    // --- Task Operations ---
    override suspend fun insertTask(task: TaskEntity) {
        photoDoDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        photoDoDao.updateTask(task)
    }

    override suspend fun deleteTask(task: TaskEntity) {
        photoDoDao.deleteTask(task)
    }

    override fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>> {
        return photoDoDao.getTasksForProject(projectId)
    }

    // --- Photo Operations ---
    override suspend fun insertPhoto(photo: PhotoEntity) {
        photoDoDao.insertPhoto(photo)
    }

    override suspend fun deletePhoto(photo: PhotoEntity) {
        photoDoDao.deletePhoto(photo)
    }

    override fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>> {
        return photoDoDao.getPhotosForProject(projectId)
    }

    // --- Relational Operations ---
    override fun getProjectWithPhotos(projectId: Long): Flow<ProjectWithPhotos?> {
        return photoDoDao.getProjectWithPhotos(projectId)
    }

    override fun getProjectDetails(projectId: Long): Flow<ProjectDetails?> {
        return photoDoDao.getProjectDetails(projectId)
    }

    override suspend fun clearAllData() {
        Log.d("PhotoDoRepoImpl", "Cleared all data")
        photoDoDao.clearPhotos()
        photoDoDao.clearTasks()
        photoDoDao.clearProjects()
        photoDoDao.clearCategories()
    }
}
