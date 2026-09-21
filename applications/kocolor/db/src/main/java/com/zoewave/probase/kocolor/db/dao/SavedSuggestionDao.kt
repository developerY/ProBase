package com.zoewave.probase.kocolor.db.dao

import androidx.room3.*
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSuggestionDao {
    @Query("SELECT * FROM saved_suggestions WHERE isSavedToCollection = 1 ORDER BY timestamp DESC")
    fun getSavedSuggestions(): Flow<List<SavedSuggestionEntity>>

    @Query("SELECT * FROM saved_suggestions WHERE isSavedToCollection = 0 ORDER BY timestamp DESC")
    fun getUnsavedSuggestions(): Flow<List<SavedSuggestionEntity>>

    @Query("SELECT * FROM saved_suggestions WHERE id = :id")
    suspend fun getSuggestionById(id: Long): SavedSuggestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSuggestion(suggestion: SavedSuggestionEntity)

    @Update
    suspend fun updateSuggestion(suggestion: SavedSuggestionEntity)

    @Delete
    suspend fun deleteSuggestion(suggestion: SavedSuggestionEntity)

    @Query("DELETE FROM saved_suggestions WHERE isSavedToCollection = 0 AND id NOT IN (SELECT id FROM saved_suggestions WHERE isSavedToCollection = 0 ORDER BY timestamp DESC LIMIT 7)")
    suspend fun trimUnsavedSuggestions()
}
