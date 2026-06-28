package com.zoewave.probase.features.health.core.domain

import com.zoewave.probase.core.model.ritual.BeautyRoutine

data class ActiveRitual(
    val routine: BeautyRoutine?,
    val title: String,
    val description: String,
    val isDaytime: Boolean
)
