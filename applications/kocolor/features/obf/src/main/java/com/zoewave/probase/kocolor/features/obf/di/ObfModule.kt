package com.zoewave.probase.kocolor.features.obf.di

import com.zoewave.probase.kocolor.features.obf.data.remote.OpenBeautyFactsApi
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
object ObfModule {

    @Provides
    @Singleton
    fun provideOpenBeautyFactsApi(): OpenBeautyFactsApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.zoewave.probase.core.network.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(OpenBeautyFactsApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(OpenBeautyFactsApi::class.java)
    }
}
