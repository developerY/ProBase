package com.zoewave.probase.photodo.features.smartadvice.domain

import kotlinx.serialization.Serializable

@Serializable
data class ProjectAdvice(
    val summary: String,
    val tips: List<String> = emptyList(),
    val potentialRisks: List<String> = emptyList(),
    val budgetAdvice: String? = null,
    val timeAdvice: String? = null,
    val suggestedChecklistItems: List<String> = emptyList()
)
