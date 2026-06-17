package com.zoewave.probase.seaweed.data

import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.data.repository.LiveAiRepository
import com.zoewave.probase.core.data.repository.BYOKLiveAiRepository
import com.zoewave.probase.features.ai.capture.domain.SmartCaptureSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindRecurringExpenseRepository(
        impl: RecurringExpenseRepositoryImpl
    ): RecurringExpenseRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        impl: UserSettingsRepositoryImpl
    ): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiConfigurationSettings(
        impl: UserSettingsRepository
    ): AiConfigurationSettings

    @Binds
    @Singleton
    abstract fun bindSmartCaptureSettings(
        impl: UserSettingsRepository
    ): SmartCaptureSettings

    @Binds
    @Singleton
    abstract fun bindBudgetTargetRepository(
        impl: BudgetTargetRepositoryImpl
    ): BudgetTargetRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindLiveAiRepository(
        impl: BYOKLiveAiRepository
    ): LiveAiRepository
}
