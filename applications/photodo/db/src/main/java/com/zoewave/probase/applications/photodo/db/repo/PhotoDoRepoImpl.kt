package com.zoewave.probase.applications.photodo.db.repo

import android.util.Log
import com.zoewave.probase.applications.photodo.db.PhotoDoDao
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjects
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjectsAndTasks
import com.zoewave.probase.applications.photodo.db.entity.ExpenseEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectWithPhotos
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.sync.TaskSyncEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [PhotoDoRepo] using [PhotoDoDao].
 */
class PhotoDoRepoImpl @Inject constructor(
    private val photoDoDao: PhotoDoDao,
    private val syncEngine: TaskSyncEngine
) : PhotoDoRepo {

    // --- Category Operations ---
    override suspend fun upsertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        photoDoDao.upsertCategory(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        photoDoDao.deleteCategory(category)
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return photoDoDao.getAllCategories()
    }

    override fun getCategoryById(categoryId: Long): Flow<CategoryEntity?> {
        return photoDoDao.getCategoryById(categoryId)
    }

    override suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        photoDoDao.updateCategory(category)
    }

    override fun getCategoriesWithProjects(): Flow<List<CategoryWithProjects>> {
        return photoDoDao.getCategoriesWithProjects()
    }

    override fun getCategoriesWithProjectsAndTasks(): Flow<List<CategoryWithProjectsAndTasks>> {
        return photoDoDao.getCategoriesWithProjectsAndTasks()
    }

    override suspend fun getOrCreateCategoryByName(name: String): Long = withContext(Dispatchers.IO) {
        // 1. Check if the category already exists
        val existingCategory = photoDoDao.getCategoryByName(name)

        if (existingCategory != null) {
            // 2. It exists! Just return its ID.
            existingCategory.categoryId
        } else {
            val newCategory = CategoryEntity(name = name)
            photoDoDao.upsertCategory(newCategory)
        }
    }

    // --- Project Operations ---
    override suspend fun upsertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        photoDoDao.upsertProject(project)
    }

    override suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        photoDoDao.deleteProject(project)
    }

    override suspend fun deleteProjectById(projectId: Long) = withContext(Dispatchers.IO) {
        photoDoDao.deleteProjectById(projectId)
    }

    override fun getProjectById(projectId: Long): Flow<ProjectEntity?> {
        return photoDoDao.getProjectById(projectId)
    }

    override suspend fun getProjectByNameAndCategory(categoryId: Long, name: String): ProjectEntity? {
        return photoDoDao.getProjectByNameAndCategory(categoryId, name)
    }

    override fun getAllProjects(): Flow<List<ProjectEntity>> {
        return photoDoDao.getAllProjects()
    }

    override fun getProjectsForCategory(categoryId: Long): Flow<List<ProjectEntity>> {
        return photoDoDao.getProjectsForCategory(categoryId)
    }

    override suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        photoDoDao.updateProject(project)
    }

    override suspend fun updateProjectUrgency(projectId: Long, isUrgent: Boolean) = withContext(Dispatchers.IO) {
        photoDoDao.updateProjectUrgency(projectId, isUrgent)
    }

    override suspend fun updateProjectFavorite(projectId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        photoDoDao.updateProjectFavorite(projectId, isFavorite)
    }

    override fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>> {
        return photoDoDao.searchProjects(searchQuery)
    }

    override fun searchProjectsWithDetails(searchQuery: String): Flow<List<ProjectDetails>> {
        return photoDoDao.searchProjectsWithDetails(searchQuery)
    }

    override fun getProjectsWithMatchingTasks(searchQuery: String): Flow<List<ProjectDetails>> {
        return photoDoDao.getProjectsWithMatchingTasks(searchQuery)
    }

    // --- Task Operations ---
    override suspend fun upsertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        val id = photoDoDao.upsertTask(task)
        syncEngine.syncTaskUpdate(task)
        id
    }

    override suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        val updatedTask = task.copy(lastModified = System.currentTimeMillis())
        photoDoDao.updateTask(updatedTask)
        syncEngine.syncTaskUpdate(updatedTask)
    }

    override suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        photoDoDao.deleteTask(task)
    }

    override fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>> {
        return photoDoDao.getTasksForProject(projectId)
    }

    override suspend fun getTaskById(taskId: Long): TaskEntity? = withContext(Dispatchers.IO) {
        photoDoDao.getTaskById(taskId)
    }

    override suspend fun getTaskBySyncId(globalSyncId: String): TaskEntity? = withContext(Dispatchers.IO) {
        photoDoDao.getTaskBySyncId(globalSyncId)
    }

    override suspend fun updateTaskStatusBySyncId(globalSyncId: String, isChecked: Boolean, lastModified: Long) = withContext(Dispatchers.IO) {
        photoDoDao.updateTaskStatusBySyncId(globalSyncId, isChecked, lastModified)
    }

    // --- Photo Operations ---
    override suspend fun upsertPhoto(photo: PhotoEntity): Long = withContext(Dispatchers.IO) {
        photoDoDao.upsertPhoto(photo)
    }

    override suspend fun deletePhoto(photo: PhotoEntity) = withContext(Dispatchers.IO) {
        photoDoDao.deletePhoto(photo)
    }

    override fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>> {
        return photoDoDao.getPhotosForProject(projectId)
    }

    override fun getAllPhotos(): Flow<List<PhotoEntity>> {
        return photoDoDao.getAllPhotos()
    }

    // --- Expense Operations ---
    override suspend fun upsertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        photoDoDao.upsertExpense(expense)
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        photoDoDao.deleteExpense(expense)
    }

    override suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        photoDoDao.updateExpense(expense)
    }

    override fun getExpensesForProject(projectId: Long): Flow<List<ExpenseEntity>> {
        return photoDoDao.getExpensesForProject(projectId)
    }

    // --- Relational Operations ---
    override fun getProjectWithPhotos(projectId: Long): Flow<ProjectWithPhotos?> {
        return photoDoDao.getProjectWithPhotos(projectId)
    }

    override fun getProjectDetails(projectId: Long): Flow<ProjectDetails?> {
        return photoDoDao.getProjectDetails(projectId)
    }

    override fun getAllProjectDetails(): Flow<List<ProjectDetails>> {
        return photoDoDao.getAllProjectDetails()
    }

    // --- Global Operations ---
    override suspend fun clearAllData() = withContext(Dispatchers.IO) {
        Log.d("PhotoDoRepoImpl", "Clearing all data using DAO @Transaction")
        photoDoDao.clearAllData()
    }
}
