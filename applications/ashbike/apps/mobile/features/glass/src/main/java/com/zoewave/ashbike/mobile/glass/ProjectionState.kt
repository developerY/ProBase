package com.zoewave.ashbike.mobile.glass

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProjectionState {
    private val _isProjecting = MutableStateFlow(false)
    val isProjecting = _isProjecting.asStateFlow()

    fun setProjecting(active: Boolean) {
        _isProjecting.value = active
    }
}