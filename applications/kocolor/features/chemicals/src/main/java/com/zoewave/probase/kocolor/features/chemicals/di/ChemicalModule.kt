package com.zoewave.probase.kocolor.features.chemicals.di

import com.zoewave.probase.kocolor.features.chemicals.data.api.PubChemApi
import com.zoewave.probase.kocolor.features.chemicals.data.repository.ChemicalRepositoryImpl
import com.zoewave.probase.kocolor.features.chemicals.domain.repository.ChemicalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChemicalModule {

    private const val BASE_URL = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/"

    @Provides
    @Singleton
    fun providePubChemApi(): PubChemApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PubChemApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChemicalRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChemicalRepository(
        impl: ChemicalRepositoryImpl
    ): ChemicalRepository
}
