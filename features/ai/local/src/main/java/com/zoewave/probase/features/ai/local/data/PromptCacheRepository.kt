package com.zoewave.probase.features.ai.local.data

import java.security.MessageDigest
import java.util.Collections
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Providers-agnostic semantic caching layer for AI execution tiers.
 * Memoizes StyleBlueprint results based on a unique fingerprint of all deterministic inputs.
 */
@Singleton
class PromptCacheRepository @Inject constructor() {

    private val cache: MutableMap<String, String> = Collections.synchronizedMap(
        object : LinkedHashMap<String, String>(50, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                return size > 50
            }
        }
    )

    /**
     * Retrieves a cached AI response for the given fingerprint.
     */
    fun get(fingerprint: String): String? {
        return cache[fingerprint]
    }

    /**
     * Caches an AI response.
     */
    fun put(fingerprint: String, result: String) {
        cache[fingerprint] = result
    }

    /**
     * Generates a SHA-256 fingerprint from the deterministic inputs to the AI model.
     * Uses Unit Separator delimiter "\u001F" to prevent boundary collisions across fields.
     */
    fun generateFingerprint(
        executionTier: String,
        promptVersion: String,
        modelVersion: String,
        retrievalPolicyVersion: String,
        appearanceTelemetry: String,
        weatherState: String,
        userIntent: String,
        minifiedManifest: String
    ): String {
        val input = listOf(
            executionTier,
            promptVersion,
            modelVersion,
            retrievalPolicyVersion,
            appearanceTelemetry,
            weatherState,
            userIntent,
            minifiedManifest
        ).joinToString("\u001F")

        return hashString(input)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Returns the current number of items in the cache.
     */
    fun size(): Int = cache.size
}
