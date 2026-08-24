package com.zoewave.probase.features.ai.firebase.di

import com.zoewave.probase.features.ai.firebase.FirebaseAiClient
import com.zoewave.probase.features.ai.firebase.FirebaseAiClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseAiModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseAiClient(
        firebaseAiClientImpl: FirebaseAiClientImpl
    ): FirebaseAiClient
}
