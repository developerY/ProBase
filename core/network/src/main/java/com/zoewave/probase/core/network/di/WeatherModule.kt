package com.zoewave.probase.core.network.di

import com.zoewave.probase.core.network.repository.weather.DemoWeatherRepoImpl
import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import com.zoewave.probase.core.network.repository.weather.WeatherRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherRepositoryModule {


    @Binds
    @Singleton
    abstract fun bindsRealWeatherRepo(
        impl: WeatherRepoImpl
    ): WeatherRepo


    @Binds
    @Singleton
    @Named("demo")
    abstract fun bindsDemoWeatherRepo(
        impl: DemoWeatherRepoImpl
    ): WeatherRepo

}