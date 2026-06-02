package com.zoewave.probase.kocolor.features.fda.data.di

import com.zoewave.probase.kocolor.features.fda.data.remote.FdaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FdaModule {

    @Provides
    @Singleton
    fun provideFdaApi(): FdaApi {
        return Retrofit.Builder()
            .baseUrl(FdaApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FdaApi::class.java)
    }
}
