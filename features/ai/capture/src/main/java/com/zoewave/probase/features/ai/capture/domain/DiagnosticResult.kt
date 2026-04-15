package com.zoewave.probase.features.ai.capture.domain

import com.zoewave.probase.core.model.tasks.SmartTaskDraft

data class DiagnosticResult<T>(
    val draft: T,
    val logs: List<String> = emptyList(),
    val error: String? = null,
    val warnings: List<String> = emptyList(),
    val engineUsed: String = "Unknown"
)
