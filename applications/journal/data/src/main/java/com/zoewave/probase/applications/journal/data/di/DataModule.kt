package com.zoewave.probase.applications.journal.data.di

import com.zoewave.probase.applications.journal.data.JournalRepository
import com.zoewave.probase.applications.journal.data.JournalRepositoryImpl
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
    abstract fun bindJournalRepository(
        journalRepositoryImpl: JournalRepositoryImpl
    ): JournalRepository
}
