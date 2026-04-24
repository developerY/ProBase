package com.zoewave.probase.kocolor.db.dao

import androidx.room3.*
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSuggestionDao {
    @Query("SELECT * FROM saved_suggestions ORDER BY timestamp DESC")
    fun getAllSuggestions(): Flow<List<SavedSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSuggestion(suggestion: SavedSuggestionEntity)

    @Delete
    suspend fun deleteSuggestion(suggestion: SavedSuggestionEntity)
}
