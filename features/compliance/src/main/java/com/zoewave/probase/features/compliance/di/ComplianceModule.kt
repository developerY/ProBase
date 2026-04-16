package com.zoewave.probase.features.compliance.di

import com.zoewave.probase.features.compliance.AgeSignalsManager
import com.zoewave.probase.features.compliance.AgeSignalsManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ComplianceModule {

    @Binds
    @Singleton
    abstract fun bindAgeSignalsManager(
        impl: AgeSignalsManagerImpl
    ): AgeSignalsManager
}
