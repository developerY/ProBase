package com.zoewave.probase.seaweed.features.cashflow.di

import com.zoewave.probase.seaweed.features.cashflow.data.LocalCashFlowRepository
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CashFlowModule {

    @Binds
    @Singleton
    abstract fun bindCashFlowRepository(
        impl: LocalCashFlowRepository
    ): CashFlowRepository
}
