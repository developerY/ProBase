package com.zoewave.probase.kocolor.fashionista.di

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import com.zoewave.probase.kocolor.fashionista.scoring.FashionistaEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FashionistaModule {

    @Binds
    @Singleton
    abstract fun bindFashionistaEvaluator(
        impl: FashionistaEngine
    ): FashionistaEvaluator
}
