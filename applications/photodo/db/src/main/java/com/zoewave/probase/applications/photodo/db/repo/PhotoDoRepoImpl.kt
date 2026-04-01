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
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of [PhotoDoRepo] using [PhotoDoDao].
 */
class PhotoDoRepoImpl @Inject constructor(
    private val photoDoDao: PhotoDoDao
) : PhotoDoRepo {

    // --- Category Operations ---
    override suspend fun upsertCategory(category: CategoryEntity): Long {
        return photoDoDao.upsertCategory(category)
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

    override fun getCategoriesWithProjectsAndTasks(): Flow<List<CategoryWithProjectsAndTasks>> {
        return photoDoDao.getCategoriesWithProjectsAndTasks()
    }

    override suspend fun getOrCreateCategoryByName(name: String): Long {
        // 1. Check if the category already exists
        val existingCategory = photoDoDao.getCategoryByName(name)

        return if (existingCategory != null) {
            // 2. It exists! Just return its ID.
            existingCategory.categoryId
        } else {
            val newCategory = CategoryEntity(name = name)
            photoDoDao.upsertCategory(newCategory)
        }
    }

    // --- Project Operations ---
    override suspend fun upsertProject(project: ProjectEntity): Long {
        return photoDoDao.upsertProject(project)
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

    override fun getAllProjects(): Flow<List<ProjectEntity>> {
        return photoDoDao.getAllProjects()
    }

    override fun getProjectsForCategory(categoryId: Long): Flow<List<ProjectEntity>> {
        return photoDoDao.getProjectsForCategory(categoryId)
    }

    override suspend fun updateProject(project: ProjectEntity) {
        photoDoDao.updateProject(project)
    }

    override suspend fun updateProjectUrgency(projectId: Long, isUrgent: Boolean) {
        photoDoDao.updateProjectUrgency(projectId, isUrgent)
    }

    override suspend fun updateProjectFavorite(projectId: Long, isFavorite: Boolean) {
        photoDoDao.updateProjectFavorite(projectId, isFavorite)
    }

    override fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>> {
        return photoDoDao.searchProjects(searchQuery)
    }

    // --- Task Operations ---
    override suspend fun upsertTask(task: TaskEntity): Long {
        return photoDoDao.upsertTask(task)
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
    override suspend fun upsertPhoto(photo: PhotoEntity): Long {
        return photoDoDao.upsertPhoto(photo)
    }

    override suspend fun deletePhoto(photo: PhotoEntity) {
        photoDoDao.deletePhoto(photo)
    }

    override fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>> {
        return photoDoDao.getPhotosForProject(projectId)
    }

    override fun getAllPhotos(): Flow<List<PhotoEntity>> {
        return photoDoDao.getAllPhotos()
    }

    // --- Expense Operations ---
    override suspend fun upsertExpense(expense: ExpenseEntity): Long {
        return photoDoDao.upsertExpense(expense)
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        photoDoDao.deleteExpense(expense)
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
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
    override suspend fun clearAllData() {
        Log.d("PhotoDoRepoImpl", "Clearing all data using DAO @Transaction")
        photoDoDao.clearAllData()
    }
}
