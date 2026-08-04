package com.zoewave.probase.kocolor.features.starterpack.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.repository.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.repository.StarterPackRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StarterPackModule {

    @Binds
    @Singleton
    abstract fun bindStarterPackRepository(
        impl: StarterPackRepositoryImpl
    ): StarterPackRepository

    companion object {
        @Provides
        @Singleton
        fun provideKocolorApiService(): KocolorApiService {
            val json = Json { ignoreUnknownKeys = true }
            val contentType = "application/json".toMediaType()
            
            return Retrofit.Builder()
                .baseUrl(KocolorApiService.BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .client(OkHttpClient.Builder().build())
                .build()
                .create(KocolorApiService::class.java)
        }
    }
}
