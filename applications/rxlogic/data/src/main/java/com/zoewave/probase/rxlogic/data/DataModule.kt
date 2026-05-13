package com.zoewave.probase.rxlogic.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsMedicationRepository(
        repository: OfflineMedicationRepository
    ): MedicationRepository
}
