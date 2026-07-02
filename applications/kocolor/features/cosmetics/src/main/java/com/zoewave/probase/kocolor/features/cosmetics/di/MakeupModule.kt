package com.zoewave.probase.kocolor.features.cosmetics.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zoewave.probase.kocolor.features.cosmetics.data.remote.MakeupApiService
import com.zoewave.probase.kocolor.features.cosmetics.data.repository.MakeupRepositoryImpl
import com.zoewave.probase.kocolor.features.cosmetics.domain.repository.MakeupRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MakeupModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideMakeupApiService(): MakeupApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(MakeupApiService.BASE_URL)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(MakeupApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MakeupRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMakeupRepository(
        impl: MakeupRepositoryImpl
    ): MakeupRepository
}
