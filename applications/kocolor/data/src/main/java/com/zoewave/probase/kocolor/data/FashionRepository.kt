package com.zoewave.probase.kocolor.data

import android.util.Log
import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.mapper.toSavedSuggestionEntity
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FashionRepository"

@Singleton
class FashionRepository @Inject constructor(
    private val fashionProfileDao: FashionProfileDao,
    private val savedSuggestionDao: SavedSuggestionDao
) : GlassBridgeRepository {
    private val _isGlassConnected = MutableStateFlow(false)
    override val isGlassConnected = _isGlassConnected.asStateFlow()

    private val _isGlassSessionActive = MutableStateFlow(false)
    override val isGlassSessionActive = _isGlassSessionActive.asStateFlow()

    private val _glassCommands = MutableSharedFlow<String>()
    override val glassCommands = _glassCommands.asSharedFlow()

    override fun updateGlassConnectionState(isConnected: Boolean) {
        _isGlassConnected.value = isConnected
    }

    override fun updateGlassSessionState(isActive: Boolean) {
        _isGlassSessionActive.value = isActive
    }

    override suspend fun sendGlassCommand(command: String) {
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

    suspend fun deleteSuggestion(id: Long) = withContext(Dispatchers.IO) {
        try {
            val entity = savedSuggestionDao.getSuggestionById(id)
            if (entity != null) {
                savedSuggestionDao.deleteSuggestion(entity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete suggestion", e)
        }
    }
}
