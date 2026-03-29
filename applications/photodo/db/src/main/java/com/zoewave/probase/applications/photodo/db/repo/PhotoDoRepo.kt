package com.zoewave.probase.applications.photodo.db.repo

import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjects
import com.zoewave.probase.applications.photodo.db.entity.ExpenseEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectWithPhotos
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface PhotoDoRepo {

    // --- Category Operations ---
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun deleteCategory(category: CategoryEntity)
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>
    suspend fun updateCategory(category: CategoryEntity)
    fun getCategoriesWithProjects(): Flow<List<CategoryWithProjects>>

    // --- Project Operations ---
    suspend fun insertProject(project: ProjectEntity): Long
    suspend fun deleteProject(project: ProjectEntity)
    suspend fun deleteProjectById(projectId: Long)
    fun getProjectById(projectId: Long): Flow<ProjectEntity?>
    fun getProjectsForCategory(categoryId: Long): Flow<List<ProjectEntity>>

    suspend fun updateProject(project: ProjectEntity)

    suspend fun updateProjectUrgency(projectId: Long, isUrgent: Boolean)
    suspend fun updateProjectFavorite(projectId: Long, isFavorite: Boolean)

    // --- Task Operations (Checklist) ---
    suspend fun insertTask(task: TaskEntity)
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>>

    // --- Photo Operations ---
    suspend fun insertPhoto(photo: PhotoEntity)
    suspend fun deletePhoto(photo: PhotoEntity)
    fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>>

    // --- Expense Operations ---
    suspend fun insertExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun updateExpense(expense: ExpenseEntity)
    fun getExpensesForProject(projectId: Long): Flow<List<ExpenseEntity>>

    // --- Relational Operations ---
    fun getProjectWithPhotos(projectId: Long): Flow<ProjectWithPhotos?>
    fun getProjectDetails(projectId: Long): Flow<ProjectDetails?>
    suspend fun clearAllData()
}
