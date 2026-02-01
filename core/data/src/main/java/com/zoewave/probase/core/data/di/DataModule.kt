package com.zoewave.probase.core.data.di

import com.zoewave.probase.core.data.repository.travel.DrivingPtsRepImp
import com.zoewave.probase.core.data.repository.travel.DrivingPtsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {


    @Singleton
    @Binds
    fun bindsMapsRepo(
        mapsRepository: DrivingPtsRepImp
    ): DrivingPtsRepository


}