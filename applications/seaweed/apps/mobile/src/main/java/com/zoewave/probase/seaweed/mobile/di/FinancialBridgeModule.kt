package com.zoewave.probase.seaweed.mobile.di

import com.zoewave.probase.core.model.FinancialContextProvider
import com.zoewave.probase.seaweed.mobile.financial.SeaweedFinancialContextProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FinancialBridgeModule {

    @Binds
    @Singleton
    abstract fun bindFinancialContextProvider(
        impl: SeaweedFinancialContextProvider
    ): FinancialContextProvider
}
