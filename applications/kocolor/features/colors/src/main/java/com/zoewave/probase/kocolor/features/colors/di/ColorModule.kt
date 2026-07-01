package com.zoewave.probase.kocolor.features.colors.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zoewave.probase.kocolor.features.colors.data.remote.ColorApiService
import com.zoewave.probase.kocolor.features.colors.data.repository.ColorRepositoryImpl
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorRepository
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
object ColorModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideColorApiService(): ColorApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(ColorApiService.BASE_URL)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ColorApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ColorRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindColorRepository(
        impl: ColorRepositoryImpl
    ): ColorRepository
}
