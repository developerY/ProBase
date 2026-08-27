package com.zoewave.probase.kocolor.data.telemetry

import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint

enum class AnchorSource { 
    USER_LOCKED, 
    USER_SELECTED, 
    AUTOMATIC_CONTEXT 
}

data class AnchorRecord(
    val id: String, 
    val name: String, 
    val source: AnchorSource, 
    val reason: String
)

data class PruningRecord(
    val initialCount: Int, 
    val afterWeather: Int, 
    val afterRotation: Int, 
    val finalEligible: Int
)

data class StyleAuditTrail(
    val requestId: String,
    var anchorRecord: AnchorRecord? = null,
    var pruningRecord: PruningRecord? = null,
    var reasoningSet: List<CandidateProvenance>? = null,
    var aiProviderUsed: String? = null,
    var tokensUsed: Int? = null,
    var finalBlueprint: StyleBlueprint? = null
)
