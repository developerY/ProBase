package com.zoewave.probase.features.ai.vision.receipt.di

import com.zoewave.probase.features.ai.vision.receipt.ReceiptEngine
import com.zoewave.probase.features.ai.vision.receipt.data.CloudReceiptEngine
import com.zoewave.probase.features.ai.vision.receipt.data.LocalReceiptEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReceiptEngineModule {

    @Binds
    @Singleton
    @Named("CloudReceipt")
    abstract fun bindCloudReceiptEngine(
        impl: CloudReceiptEngine
    ): ReceiptEngine

    @Binds
    @Singleton
    @Named("LocalReceipt")
    abstract fun bindLocalReceiptEngine(
        impl: LocalReceiptEngine
    ): ReceiptEngine
}
