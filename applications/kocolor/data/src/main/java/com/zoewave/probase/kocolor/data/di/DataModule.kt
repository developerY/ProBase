package com.zoewave.probase.kocolor.data.di

import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.data.repository.LiveAiRepository
import com.zoewave.probase.core.data.repository.RitualRepository
import com.zoewave.probase.core.data.repository.BYOKLiveAiRepository
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindGlassBridgeRepository(impl: FashionRepository): GlassBridgeRepository

    @Binds
    @Singleton
    abstract fun bindRitualRepository(impl: RitualRepositoryImpl): RitualRepository

    @Binds
    @Singleton
    abstract fun bindLiveAiRepository(impl: BYOKLiveAiRepository): LiveAiRepository

    @Binds
    @Singleton
    abstract fun bindRotationRepository(impl: RotationRepositoryImpl): RotationRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    companion object {
        @Provides
        @Singleton
        fun provideFashionSessionRepository(): FashionSessionRepository {
            return FashionSessionRepository()
        }
    }
}
