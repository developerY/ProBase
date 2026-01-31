package com.zoewave.probase.core.network.di

import com.zoewave.probase.core.network.repository.travel.compass.CompassRepository
import com.zoewave.probase.core.network.repository.travel.compass.CompassRepositoryAccMagImpl
import com.zoewave.probase.core.network.repository.travel.compass.DemoCompassRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class CompassRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRealCompassRepository(
        //impl: CompassRepositoryRotVecImpl
        impl: CompassRepositoryAccMagImpl
    ): CompassRepository

    @Binds
    @Singleton
    @Named("demo")
    abstract fun bindDemoCompassRepository(
        impl: DemoCompassRepositoryImpl
    ): CompassRepository
}
