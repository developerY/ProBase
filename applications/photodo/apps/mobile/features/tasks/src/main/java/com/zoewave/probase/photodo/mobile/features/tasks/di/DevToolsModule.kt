package com.zoewave.probase.photodo.mobile.features.tasks.di

import com.zoewave.probase.photodo.mobile.features.tasks.domain.ReleaseTaskDevTools
import com.zoewave.probase.photodo.mobile.features.tasks.domain.TaskDevTools
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DevToolsModuleRel {
    @Binds
    abstract fun bindDevTools(impl: ReleaseTaskDevTools): TaskDevTools
}