package com.zoewave.probase.kocolor.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zoewave.probase.kocolor.data.remote.KocolorApiService
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
object NetworkModule {

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
