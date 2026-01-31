package com.zoewave.probase.core.network.di

import com.zoewave.probase.core.network.repository.nfc.NfcRepository
import com.zoewave.probase.core.network.repository.nfc.NfcRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NfcModule {

    @Binds
    @Singleton
    abstract fun bindNFCRepository(
        impl: NfcRepositoryImpl
    ): NfcRepository
}