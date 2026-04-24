package com.zoewave.probase.kocolor.db.di

import android.content.Context
import androidx.room3.Room
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindAiConfigurationSettings(impl: KoColorSettings): AiConfigurationSettings

    @Binds
    @Singleton
    abstract fun bindSmartCaptureSettings(impl: KoColorSettings): SmartCaptureSettings

    companion object {
        @Provides
        @Singleton
        fun provideKoColorDatabase(@ApplicationContext context: Context): KoColorDatabase {
            return Room.databaseBuilder(
                context,
                KoColorDatabase::class.java,
                "kocolor_database"
            ).build()
        }

        @Provides
        fun provideFashionProfileDao(db: KoColorDatabase): FashionProfileDao = db.fashionProfileDao

        @Provides
        fun provideSavedSuggestionDao(db: KoColorDatabase): SavedSuggestionDao = db.savedSuggestionDao
    }
}
