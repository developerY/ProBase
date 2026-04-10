package com.zoewave.probase.photodo.mobile.features.tasks.ui

sealed interface TasksSideEffect {
    data object NavigateBack : TasksSideEffect
}
