package com.zoewave.probase.kocolor.data.telemetry

import android.util.Log
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleAuditLogger @Inject constructor() {

    private val trails = ConcurrentHashMap<String, StyleAuditTrail>()

    fun startRequest(requestId: String) {
        trails[requestId] = StyleAuditTrail(requestId)
    }

    fun logAnchorResolution(requestId: String, anchor: ClothingItem, source: AnchorSource, reason: String) {
        trails[requestId]?.anchorRecord = AnchorRecord(
            id = "w_${anchor.internalId}",
            name = anchor.name,
            source = source,
            reason = reason
        )
    }

    fun logDeterministicPruning(requestId: String, record: PruningRecord) {
        trails[requestId]?.pruningRecord = record
    }

    fun logReasoningSet(requestId: String, candidates: List<CandidateProvenance>) {
        trails[requestId]?.reasoningSet = candidates
    }

    fun logAiExecution(requestId: String, providerId: String, tokens: Int, blueprint: StyleBlueprint) {
        trails[requestId]?.apply {
            aiProviderUsed = providerId
            tokensUsed = tokens
            finalBlueprint = blueprint
        }
    }

    fun printAuditTrail(requestId: String) {
        val trail = trails[requestId] ?: return
        
        val output = StringBuilder().apply {
            appendLine("==================================================")
            appendLine("              KOCOLOR AUDIT TRAIL                 ")
            appendLine("==================================================")
            
            appendLine("[1] ANCHOR ESTABLISHMENT")
            trail.anchorRecord?.let {
                appendLine("    Source: ${it.source.name}")
                appendLine("    Item: [${it.id}] \"${it.name}\"")
                appendLine("    Reason: ${it.reason}")
            } ?: appendLine("    NO ANCHOR RESOLVED")
            appendLine()

            appendLine("[2] DETERMINISTIC PRUNING")
            trail.pruningRecord?.let {
                appendLine("    Initial Wardrobe: ${it.initialCount} items")
                appendLine("    Passed Weather/Occasion: ${it.afterWeather} items")
                appendLine("    Passed Rotation/Availability: ${it.afterRotation} items")
                appendLine("    Final Eligible: ${it.finalEligible} items")
            } ?: appendLine("    NO PRUNING RECORDED")
            appendLine()

            appendLine("[3] MATHEMATICAL COLOR & ROLE SCORING (Top ${trail.reasoningSet?.size ?: 0})")
            trail.reasoningSet?.forEach { prov ->
                val id = "w_${prov.item.internalId}"
                val score = "%.2f".format(prov.totalScore)
                appendLine("    - [$id] \"${prov.item.name}\" (Score: $score) -> Reason: ${prov.retrievalReason}")
            }
            appendLine()

            appendLine("[4] AI AESTHETIC SYNTHESIS")
            appendLine("    Provider: ${trail.aiProviderUsed ?: "N/A"} (Tokens: ${trail.tokensUsed ?: 0})")
            appendLine("    Selected Clothing: ${trail.finalBlueprint?.selectedClothingIds}")
            appendLine("    Selected Cosmetics: ${trail.finalBlueprint?.selectedCosmeticIds}")
            appendLine("    AI Rationale: \"${trail.finalBlueprint?.rationale ?: "N/A"}\"")
            appendLine("==================================================")
        }.toString()

        Log.d("KoColor_Audit", output)
        
        // Optional: remove after printing to keep memory clean
        trails.remove(requestId)
    }
}
