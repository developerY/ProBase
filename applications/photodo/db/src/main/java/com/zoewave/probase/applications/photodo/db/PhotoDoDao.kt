package com.zoewave.probase.applications.photodo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjects
import com.zoewave.probase.applications.photodo.db.entity.ExpenseEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectWithPhotos
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDoDao {

    // -- Delete All

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity) : Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE projectId = :projectId")
    fun getExpensesForProject(projectId: Long): Flow<List<ExpenseEntity>>

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<CategoryEntity?>

    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithProjects(): Flow<List<CategoryWithProjects>>

    // --- Project Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity) : Long

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE projectId = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectById(projectId: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE categoryId = :categoryId ORDER BY creationDate DESC")
    fun getProjectsForCategory(categoryId: Long): Flow<List<ProjectEntity>>

    @Query("UPDATE projects SET isUrgent = :isUrgent WHERE projectId = :projectId")
    suspend fun updateProjectUrgency(projectId: Long, isUrgent: Boolean)

    @Query("UPDATE projects SET isFavorite = :isFavorite WHERE projectId = :projectId")
    suspend fun updateProjectFavorite(projectId: Long, isFavorite: Boolean)

    // --- Task Operations (Checklist) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>>

    // --- Photo Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>>

    // --- Relational Query ---

    @Transaction
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectWithPhotos(projectId: Long): Flow<ProjectWithPhotos?>

    @Transaction
    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectDetails(projectId: Long): Flow<ProjectDetails?>
}
