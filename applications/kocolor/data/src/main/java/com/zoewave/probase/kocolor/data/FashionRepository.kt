package com.zoewave.probase.kocolor.data

import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.SavedAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FashionRepository @Inject constructor(
    private val fashionProfileDao: FashionProfileDao,
    private val savedSuggestionDao: SavedSuggestionDao
) {
    fun getProfile(): Flow<FashionProfile?> {
        return fashionProfileDao.getProfile().map { entity ->
            entity?.let {
                FashionProfile(
                    id = it.id,
                    seasonalType = it.seasonalType,
                    undertone = it.undertone,
                    skinToneHex = it.skinToneHex,
                    eyeColor = it.eyeColor,
                    hairColor = it.hairColor,
                    notes = it.notes,
                    recommendedPalette = it.recommendedPalette
                )
            }
        }
    }

    suspend fun saveProfile(profile: FashionProfile) {
        fashionProfileDao.saveProfile(
            FashionProfileEntity(
                id = profile.id,
                seasonalType = profile.seasonalType,
                undertone = profile.undertone,
                skinToneHex = profile.skinToneHex,
                eyeColor = profile.eyeColor,
                hairColor = profile.hairColor,
                notes = profile.notes,
                recommendedPalette = profile.recommendedPalette
            )
        )
    }

    fun getSavedSuggestions(): Flow<List<SavedAnalysis>> {
        return savedSuggestionDao.getAllSuggestions().map { list ->
            list.map { 
                SavedAnalysis(
                    id = it.id,
                    timestamp = it.timestamp,
                    advice = it.advice
                )
            }
        }
    }

    suspend fun getSuggestionById(id: Long): SavedAnalysis? {
        return savedSuggestionDao.getSuggestionById(id)?.let {
            SavedAnalysis(
                id = it.id,
                timestamp = it.timestamp,
                advice = it.advice
            )
        }
    }

    suspend fun saveSuggestion(advice: FashionAdvice) {
        savedSuggestionDao.saveSuggestion(
            SavedSuggestionEntity(
                timestamp = System.currentTimeMillis(),
                advice = advice
            )
        )
    }
}
