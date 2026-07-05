package com.zoewave.probase.features.readers.ocr.domain.parser

/**
 * Data structure for the chemically-cleaned ingredient payload.
 */
data class ParsedIngredients(
    val active: List<String> = emptyList(),
    val inactive: List<String> = emptyList()
)

/**
 * IngredientParser: Triage and cleanup of raw OCR text for chemical data integrity.
 * Uses legal FDA anchor hunting and early-exit stop-word guardrails.
 */
object IngredientParser {

    private val ACTIVE_REGEX = Regex("(?i)active\\s+ingredients?[:\\-]?")
    private val INACTIVE_REGEX = Regex("(?i)(?:inactive|other)\\s+ingredients?[:\\-]?")
    private val GENERIC_REGEX = Regex("(?i)ingredients?[:\\-]?")

    // The Cut-Off Guardrail: Immediate cessation upon hitting manufacturer/footer noise.
    private val FOOTER_STOP_WORDS = listOf(
        "distributed by", "dist. by", "questions?", "questions or comments",
        "made in", "www.", "1-800", "caution:", "warning:", "directions:",
        "manufactured", "for external use", "keep out of"
    )

    fun parse(rawText: String): ParsedIngredients {
        if (rawText.isBlank()) return ParsedIngredients()

        // 1. Pre-sanitize: Flatten line breaks into a single space for easier regex matching
        val flatText = rawText.replace("\n", " ").replace("\r", " ")

        // 2. Identify the starting anchors
        val activeMatch = ACTIVE_REGEX.find(flatText)
        val inactiveMatch = INACTIVE_REGEX.find(flatText)
        val genericMatch = GENERIC_REGEX.find(flatText)

        return when {
            // Case A: Standard split (Active vs Inactive)
            activeMatch != null && inactiveMatch != null -> {
                val activeBlock = extractBlock(flatText, activeMatch.range.last + 1, inactiveMatch.range.first)
                val inactiveBlock = extractBlock(flatText, inactiveMatch.range.last + 1, flatText.length)
                ParsedIngredients(active = tokenize(activeBlock), inactive = tokenize(inactiveBlock))
            }
            // Case B: Inactive/Generic only
            inactiveMatch != null -> {
                val inactiveBlock = extractBlock(flatText, inactiveMatch.range.last + 1, flatText.length)
                ParsedIngredients(inactive = tokenize(inactiveBlock))
            }
            // Case C: Generic "Ingredients" anchor
            genericMatch != null -> {
                val block = extractBlock(flatText, genericMatch.range.last + 1, flatText.length)
                ParsedIngredients(inactive = tokenize(block))
            }
            // Case D: No anchors found - attempt raw tokenization of full text (Heuristic fallback)
            else -> {
                ParsedIngredients(inactive = tokenize(extractBlock(flatText, 0, flatText.length)))
            }
        }
    }

    /**
     * Extracts a substring block while enforcing the early-exit stop-word guardrail.
     */
    private fun extractBlock(text: String, start: Int, endLimit: Int): String {
        if (start >= text.length) return ""
        
        val actualEnd = minOf(text.length, endLimit)
        val candidateBlock = text.substring(start, actualEnd)
        
        // Find the earliest stop word in this candidate block
        var stopIndex = candidateBlock.length
        for (stopWord in FOOTER_STOP_WORDS) {
            val idx = candidateBlock.indexOf(stopWord, ignoreCase = true)
            if (idx != -1 && idx < stopIndex) {
                stopIndex = idx
            }
        }
        
        return candidateBlock.substring(0, stopIndex).trim()
    }

    /**
     * Splits string by commas, trims, and removes trailing punctuation/artifacts.
     */
    private fun tokenize(block: String): List<String> {
        return block.split(",")
            .map { it.trim() }
            .map { it.removeSuffix(".").removeSuffix(";") }
            .filter { it.isNotBlank() && it.length > 2 }
            .filter { !it.contains(Regex("[0-9]{3}-[0-9]{3}")) } // Extra layer to catch stray phone fragments
    }
}
