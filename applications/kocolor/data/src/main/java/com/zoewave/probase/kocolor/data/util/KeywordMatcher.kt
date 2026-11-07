package com.zoewave.probase.kocolor.data.util

import java.util.Collections

object KeywordMatcher {
    private val WORD_REGEX = Regex("\\b[a-z0-9]+\\b")

    val STOP_WORDS: Set<String> = setOf(
        "a", "an", "the", "in", "on", "for", "with", "to", "of",
        "and", "or", "is", "are", "at", "by", "from", "as", "into",
        "my", "me", "i", "want", "need", "something", "style", "look", "outfit"
    )

    /**
     * Normalizes a lowercase word into ONE canonical stem so singular and plural
     * forms ("boot"/"boots", "dress"/"dresses", "accessory"/"accessories")
     * map to the exact same token without double-counting.
     */
    fun normalizeStem(word: String): String = when {
        word.length > 4 && word.endsWith("resses") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("ches") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("shes") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("xes") -> word.removeSuffix("es")
        word.length > 4 && word.endsWith("ies") -> word.removeSuffix("ies") + "y"
        word.length > 3 && word.endsWith("s") && !word.endsWith("ss") && !word.endsWith("us") -> word.removeSuffix("s")
        else -> word
    }

    /**
     * Returns a Set of unique canonical stems (length >= 2, stop-words removed)
     * for unordered token-overlap scoring.
     */
    fun tokenize(text: String): Set<String> {
        if (text.isBlank()) return emptySet()
        return WORD_REGEX.findAll(text.lowercase())
            .map { it.value }
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .map { normalizeStem(it) }
            .toSet()
    }

    /**
     * Returns an ordered List of canonical stems (preserving word positions, including
     * intervening words) so multi-word phrase checks require contiguous adjacency.
     */
    fun tokenizeSequence(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return WORD_REGEX.findAll(text.lowercase())
            .map { normalizeStem(it.value) }
            .toList()
    }

    /**
     * Returns true if [targetText] contains [keyword] as a whole word or contiguous
     * multi-word phrase after canonical stem normalization.
     * Rejects queries that consist solely of stop-words or single-character tokens.
     */
    fun containsWholeWord(targetText: String, keyword: String): Boolean {
        if (targetText.isBlank() || keyword.isBlank()) return false
        val rawWords = WORD_REGEX.findAll(keyword.lowercase()).map { it.value }.toList()
        if (rawWords.isEmpty() || rawWords.all { it.length < 2 || it in STOP_WORDS }) return false
        val querySeq = rawWords.map { normalizeStem(it) }
        val targetSeq = tokenizeSequence(targetText)
        if (targetSeq.isEmpty()) return false
        return Collections.indexOfSubList(targetSeq, querySeq) != -1
    }

    /**
     * Counts how many distinct non-stopword canonical stems from [intentQuery]
     * appear in [itemText]. Each concept is counted at most once.
     */
    fun countMatchingTokens(itemText: String, intentQuery: String): Int {
        val queryTokens = tokenize(intentQuery)
        if (queryTokens.isEmpty()) return 0
        val targetTokens = tokenize(itemText)
        return queryTokens.count { it in targetTokens }
    }

    fun hasAnyMatchingToken(itemText: String, intentQuery: String): Boolean =
        countMatchingTokens(itemText, intentQuery) > 0
}
