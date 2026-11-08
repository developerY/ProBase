package com.zoewave.probase.features.ai.core.util

object JsonSanitizer {
    private val CODE_FENCE_REGEX = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)

    /**
     * Extracts the first complete, balanced JSON object "{...}" from [raw],
     * stripping markdown code fences and ignoring '{' or '}' characters that
     * occur inside quoted JSON string literals. Returns null if no balanced
     * JSON object exists.
     */
    fun extractFirstJsonObject(raw: String): String? {
        if (raw.isBlank()) return null
        val unfenced = CODE_FENCE_REGEX.find(raw)?.groupValues?.getOrNull(1)?.trim() ?: raw.trim()

        var startIndex = -1
        var depth = 0
        var inString = false
        var isEscaped = false

        for (i in unfenced.indices) {
            val ch = unfenced[i]
            if (isEscaped) {
                isEscaped = false
                continue
            }
            if (ch == '\\' && inString) {
                isEscaped = true
                continue
            }
            if (ch == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (ch == '{') {
                    if (depth == 0) startIndex = i
                    depth++
                } else if (ch == '}') {
                    if (depth > 0) {
                        depth--
                        if (depth == 0 && startIndex != -1) {
                            return unfenced.substring(startIndex, i + 1)
                        }
                    }
                }
            }
        }
        return null
    }
}
