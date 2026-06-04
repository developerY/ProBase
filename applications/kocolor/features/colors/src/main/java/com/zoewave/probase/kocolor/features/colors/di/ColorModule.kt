package com.zoewave.probase.kocolor.features.colors.di

import com.zoewave.probase.kocolor.features.colors.data.remote.ColorApi
import com.zoewave.probase.kocolor.features.colors.data.repository.ColorRepositoryImpl
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ColorModule {

    @Provides
    @Singleton
    fun provideColorApi(): ColorApi {
        return Retrofit.Builder()
            .baseUrl(ColorApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ColorApi::class.java)
    }

    @Provides
    @Singleton
    fun provideColorRepository(api: ColorApi): ColorRepository {
        return ColorRepositoryImpl(api)
    }
}
