package com.zoewave.probase.features.health.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zoewave.probase.features.health.data.remote.RxNavApiService
import com.zoewave.probase.features.health.data.repository.ClinicalIngredientRepository
import com.zoewave.probase.features.health.data.repository.ClinicalIngredientRepositoryImpl
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
abstract class HealthDataModule {

    @Binds
    @Singleton
    abstract fun bindClinicalIngredientRepository(
        impl: ClinicalIngredientRepositoryImpl
    ): ClinicalIngredientRepository

    companion object {

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder().build()
        }

        @Provides
        @Singleton
        fun provideRxNavApiService(
            json: Json,
            okHttpClient: OkHttpClient
        ): RxNavApiService {
            return Retrofit.Builder()
                .baseUrl(RxNavApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(RxNavApiService::class.java)
        }
    }
}
