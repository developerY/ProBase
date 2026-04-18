package com.zoewave.probase.features.ai.firebase.di

import com.zoewave.probase.features.ai.firebase.data.FirebaseLiveSessionManager
import com.zoewave.probase.features.ai.firebase.domain.GeminiFirebaseManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseAiModule {

    @Provides
    @Singleton
    fun provideGeminiFirebaseManager(): GeminiFirebaseManager {
        return GeminiFirebaseManager()
    }

    @Provides
    fun provideFirebaseLiveSessionManager(
        geminiFirebaseManager: GeminiFirebaseManager
    ): FirebaseLiveSessionManager {
        return FirebaseLiveSessionManager(geminiFirebaseManager)
    }
}
