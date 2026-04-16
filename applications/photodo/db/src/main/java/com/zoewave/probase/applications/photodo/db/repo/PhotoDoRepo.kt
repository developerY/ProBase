package com.zoewave.probase.applications.photodo.db.repo

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

/**
 * Repository for the PhotoDo application.
 * Manages data access for categories, projects, photos, tasks, and expenses.
 */
interface PhotoDoRepo {

    // --- Category Operations ---
    suspend fun upsertCategory(category: CategoryEntity): Long
    suspend fun deleteCategory(category: CategoryEntity)
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>
    suspend fun updateCategory(category: CategoryEntity)
    fun getCategoriesWithProjects(): Flow<List<CategoryWithProjects>>
    fun getCategoriesWithProjectsAndTasks(): Flow<List<CategoryWithProjectsAndTasks>>
    suspend fun getOrCreateCategoryByName(name: String): Long

    // --- Project Operations ---
    suspend fun upsertProject(project: ProjectEntity): Long
    suspend fun deleteProject(project: ProjectEntity)
    suspend fun deleteProjectById(projectId: Long)
    fun getProjectById(projectId: Long): Flow<ProjectEntity?>
    fun getAllProjects(): Flow<List<ProjectEntity>>
    fun getProjectsForCategory(categoryId: Long): Flow<List<ProjectEntity>>
    suspend fun updateProject(project: ProjectEntity)
    suspend fun updateProjectUrgency(projectId: Long, isUrgent: Boolean)
    suspend fun updateProjectFavorite(projectId: Long, isFavorite: Boolean)
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>>
    fun searchProjectsWithDetails(searchQuery: String): Flow<List<ProjectDetails>>
    fun getProjectsWithMatchingTasks(searchQuery: String): Flow<List<ProjectDetails>>

    // --- Task Operations (Checklist) ---
    suspend fun upsertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>>
    suspend fun getTaskById(taskId: Long): TaskEntity?
    suspend fun getTaskBySyncId(globalSyncId: String): TaskEntity?
    suspend fun updateTaskStatusBySyncId(globalSyncId: String, isChecked: Boolean, lastModified: Long)

    // --- Photo Operations ---
    suspend fun upsertPhoto(photo: PhotoEntity): Long
    suspend fun deletePhoto(photo: PhotoEntity)
    fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>>
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    // --- Expense Operations ---
    suspend fun upsertExpense(expense: ExpenseEntity): Long
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun updateExpense(expense: ExpenseEntity)
    fun getExpensesForProject(projectId: Long): Flow<List<ExpenseEntity>>

    // --- Relational Operations ---
    fun getProjectWithPhotos(projectId: Long): Flow<ProjectWithPhotos?>
    fun getProjectDetails(projectId: Long): Flow<ProjectDetails?>
    fun getAllProjectDetails(): Flow<List<ProjectDetails>>

    // --- Global Operations ---
    suspend fun clearAllData()
}
