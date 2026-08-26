package com.zoewave.probase.kocolor.data.di

import com.zoewave.probase.kocolor.data.usecase.CapabilityRouter
import com.zoewave.probase.kocolor.data.usecase.CapabilityRouterImpl
import com.zoewave.probase.kocolor.data.usecase.DeterministicStyleEngine
import com.zoewave.probase.kocolor.data.usecase.HeuristicStyleEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiRoutingComponentsModule {

    @Binds
    @Singleton
    abstract fun bindCapabilityRouter(
        impl: CapabilityRouterImpl
    ): CapabilityRouter

    @Binds
    @Singleton
    abstract fun bindDeterministicStyleEngine(
        impl: HeuristicStyleEngine
    ): DeterministicStyleEngine
}
