package com.zoewave.probase.applications.photodo.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
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
 * Data Access Object for the PhotoDo application.
 * Follows Modern Android Development (MAD) best practices:
 * - Uses [Flow] for observable queries.
 * - Uses [suspend] for one-shot asynchronous operations.
 * - Uses [@Upsert] to simplify insert/update logic.
 * - Includes [@Transaction] for complex multi-query operations.
 */
@Dao
interface PhotoDoDao {

    // --- Global Operations ---

    @Transaction
    suspend fun clearAllData() {
        clearPhotos()
        clearTasks()
        clearExpenses()
        clearProjects()
        clearCategories()
    }

    @Query("DELETE FROM photos")
    suspend fun clearPhotos()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM projects")
    suspend fun clearProjects()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    // --- Category Operations ---

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity): Long

    @Upsert
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithProjects(): Flow<List<CategoryWithProjects>>

    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithProjectsAndTasks(): Flow<List<CategoryWithProjectsAndTasks>>

    // --- Project Operations ---

    @Upsert
    suspend fun upsertProject(project: ProjectEntity): Long

    @Upsert
    suspend fun upsertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE projectId = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectById(projectId: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE categoryId = :categoryId AND name = :name LIMIT 1")
    suspend fun getProjectByNameAndCategory(categoryId: Long, name: String): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY creationDate DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE categoryId = :categoryId ORDER BY creationDate DESC")
    fun getProjectsForCategory(categoryId: Long): Flow<List<ProjectEntity>>

    @Query("UPDATE projects SET isUrgent = :isUrgent WHERE projectId = :projectId")
    suspend fun updateProjectUrgency(projectId: Long, isUrgent: Boolean)

    @Query("UPDATE projects SET isFavorite = :isFavorite WHERE projectId = :projectId")
    suspend fun updateProjectFavorite(projectId: Long, isFavorite: Boolean)

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%'")
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>>

    @Transaction
    @Query("SELECT * FROM projects WHERE name LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%'")
    fun searchProjectsWithDetails(searchQuery: String): Flow<List<ProjectDetails>>

    @Transaction
    @Query("SELECT * FROM projects WHERE projectId IN (SELECT DISTINCT projectId FROM tasks WHERE text LIKE '%' || :searchQuery || '%')")
    fun getProjectsWithMatchingTasks(searchQuery: String): Flow<List<ProjectDetails>>

    // --- Task Operations (Checklist) ---

    @Upsert
    suspend fun upsertTask(task: TaskEntity): Long

    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE globalSyncId = :globalSyncId LIMIT 1")
    suspend fun getTaskBySyncId(globalSyncId: String): TaskEntity?

    @Query("UPDATE tasks SET isChecked = :isChecked, lastModified = :lastModified WHERE globalSyncId = :globalSyncId")
    suspend fun updateTaskStatusBySyncId(globalSyncId: String, isChecked: Boolean, lastModified: Long)

    // --- Photo Operations ---

    @Upsert
    suspend fun upsertPhoto(photo: PhotoEntity): Long

    @Upsert
    suspend fun upsertPhotos(photos: List<PhotoEntity>)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    // --- Expense Operations ---

    @Upsert
    suspend fun upsertExpense(expense: ExpenseEntity): Long

    @Upsert
    suspend fun upsertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE projectId = :projectId")
    fun getExpensesForProject(projectId: Long): Flow<List<ExpenseEntity>>

    // --- Relational Query ---

    @Transaction
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectWithPhotos(projectId: Long): Flow<ProjectWithPhotos?>

    @Transaction
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectDetails(projectId: Long): Flow<ProjectDetails?>

    @Transaction
    @Query("SELECT * FROM projects")
    fun getAllProjectDetails(): Flow<List<ProjectDetails>>
}
