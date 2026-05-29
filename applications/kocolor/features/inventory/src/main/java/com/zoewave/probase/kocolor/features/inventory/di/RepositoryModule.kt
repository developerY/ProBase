package com.zoewave.probase.kocolor.features.inventory.di

import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.features.inventory.data.repository.CosmeticInventoryRepositoryImpl
import com.zoewave.probase.kocolor.features.inventory.data.repository.WardrobeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWardrobeRepository(
        impl: WardrobeRepositoryImpl
    ): WardrobeRepository

    @Binds
    @Singleton
    abstract fun bindCosmeticInventoryRepository(
        impl: CosmeticInventoryRepositoryImpl
    ): CosmeticInventoryRepository
}
