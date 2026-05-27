package com.zoewave.probase.kocolor.data

import android.util.Log
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.mapper.toSavedSuggestionEntity
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.SavedAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FashionRepository"

@Singleton
class FashionRepository @Inject constructor(
    private val fashionProfileDao: FashionProfileDao,
    private val savedSuggestionDao: SavedSuggestionDao
) {
    private val _isGlassConnected = MutableStateFlow(false)
    val isGlassConnected = _isGlassConnected.asStateFlow()

    private val _isGlassSessionActive = MutableStateFlow(false)
    val isGlassSessionActive = _isGlassSessionActive.asStateFlow()

    private val _glassCommands = MutableSharedFlow<String>()
    val glassCommands = _glassCommands.asSharedFlow()

    fun updateGlassConnectionState(isConnected: Boolean) {
        _isGlassConnected.value = isConnected
    }

    fun updateGlassSessionState(isActive: Boolean) {
        _isGlassSessionActive.value = isActive
    }

    suspend fun sendGlassCommand(command: String) {
        _glassCommands.emit(command)
    }

    fun getProfile(): Flow<FashionProfile?> {
        return fashionProfileDao.getProfile()
            .map { it?.toModel() }
            .catch { e ->
                Log.e(TAG, "Error fetching fashion profile", e)
                emit(null)
            }
    }

    suspend fun saveProfile(profile: FashionProfile) = withContext(Dispatchers.IO) {
        try {
            fashionProfileDao.saveProfile(profile.toEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save fashion profile", e)
        }
    }

    fun getSavedSuggestions(): Flow<List<SavedAnalysis>> {
        return savedSuggestionDao.getAllSuggestions()
            .map { list -> list.map { it.toModel() } }
            .catch { e ->
                Log.e(TAG, "Error fetching saved suggestions", e)
                emit(emptyList())
            }
    }

    suspend fun getSuggestionById(id: Long): SavedAnalysis? = withContext(Dispatchers.IO) {
        try {
            savedSuggestionDao.getSuggestionById(id)?.toModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching suggestion by id: $id", e)
            null
        }
    }

    suspend fun saveSuggestion(advice: FashionAdvice) = withContext(Dispatchers.IO) {
        try {
            savedSuggestionDao.saveSuggestion(advice.toSavedSuggestionEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save suggestion", e)
        }
    }
}
