package com.zoewave.probase.kocolor.data.di

import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
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
    fun provideFashionSessionRepository(): FashionSessionRepository {
        return FashionSessionRepository()
    }
}
