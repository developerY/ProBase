package com.zoewave.probase.core.data.di

import com.zoewave.probase.core.data.repository.bike.BikeConnectivityRepository
import com.zoewave.probase.core.data.repository.bike.BikeConnectivityRepositoryImpl
import com.zoewave.probase.core.data.repository.bike.DemoBikeConnectivityRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideRealBikeConnectivityRepository(
        //nfcReader: NfcReader,
        //bleAdapter: BleAdapter
    ): BikeConnectivityRepository = BikeConnectivityRepositoryImpl()//nfcReader, bleAdapter)

    @Singleton
    @Provides
    @Named("demo")
    fun provideDemoBikeConnectivityRepository(): BikeConnectivityRepository =
        DemoBikeConnectivityRepositoryImpl()

}
