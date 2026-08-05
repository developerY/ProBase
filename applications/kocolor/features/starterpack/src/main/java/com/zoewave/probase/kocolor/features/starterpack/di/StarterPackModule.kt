package com.zoewave.probase.kocolor.features.starterpack.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepository
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepositoryImpl
import com.zoewave.probase.kocolor.features.starterpack.domain.security.SignatureVerifier
import com.zoewave.probase.kocolor.features.starterpack.domain.security.SignatureVerifierImpl
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
    abstract fun bindPackSyncRepository(
        impl: PackSyncRepositoryImpl
    ): PackSyncRepository

    @Binds
    @Singleton
    abstract fun bindSignatureVerifier(
        impl: SignatureVerifierImpl
    ): SignatureVerifier

    companion object {
        @Provides
        @Singleton
        fun provideJson(): Json = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }

        @Provides
        @Singleton
        fun provideKocolorApiService(json: Json): KocolorApiService {
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
