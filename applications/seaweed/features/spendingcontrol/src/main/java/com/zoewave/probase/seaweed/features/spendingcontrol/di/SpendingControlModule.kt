package com.zoewave.probase.seaweed.features.spendingcontrol.di

import com.zoewave.probase.seaweed.features.spendingcontrol.data.InMemoryEnvelopeRepository
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.DecisionEngine
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.EnvelopeRepository
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.RealTimeDecisionEngine
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.RulesBasedClassifier
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.TransactionClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpendingControlModule {

    @Binds
    @Singleton
    abstract fun bindEnvelopeRepository(
        impl: InMemoryEnvelopeRepository
    ): EnvelopeRepository

    @Binds
    @Singleton
    abstract fun bindDecisionEngine(
        impl: RealTimeDecisionEngine
    ): DecisionEngine

    @Binds
    @Singleton
    abstract fun bindTransactionClassifier(
        impl: RulesBasedClassifier
    ): TransactionClassifier
}
