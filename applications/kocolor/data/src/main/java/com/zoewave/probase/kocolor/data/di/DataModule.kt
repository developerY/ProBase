package com.zoewave.probase.kocolor.data.di

import android.content.Context
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.data.engine.WardrobeColorEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFashionRepository(
        fashionProfileDao: FashionProfileDao,
        savedSuggestionDao: SavedSuggestionDao
    ): FashionRepository {
        return FashionRepository(fashionProfileDao, savedSuggestionDao)
    }

    @Provides
    @Singleton
    fun provideWardrobeRepository(
        clothingDao: ClothingDao,
        colorEngine: WardrobeColorEngine,
        @ApplicationContext context: Context
    ): WardrobeRepository {
        return WardrobeRepository(clothingDao, colorEngine, context)
    }

    @Provides
    @Singleton
    fun provideFashionSessionRepository(): FashionSessionRepository {
        return FashionSessionRepository()
    }
}
