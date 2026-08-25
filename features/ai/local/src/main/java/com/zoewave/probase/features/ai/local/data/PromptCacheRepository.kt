package com.zoewave.probase.features.ai.local.data

import android.util.LruCache
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Providers-agnostic semantic caching layer for AI execution tiers.
 * Memoizes StyleBlueprint results based on a unique fingerprint of all deterministic inputs.
 */
@Singleton
class PromptCacheRepository @Inject constructor() {

    private val cache = LruCache<String, String>(50)

    /**
     * Retrieves a cached AI response for the given fingerprint.
     */
    fun get(fingerprint: String): String? {
        return cache.get(fingerprint)
    }

    /**
     * Caches an AI response.
     */
    fun put(fingerprint: String, result: String) {
        cache.put(fingerprint, result)
    }

    /**
     * Generates a SHA-256 fingerprint from the deterministic inputs to the AI model.
     * Any change in these inputs (e.g., weather, wardrobe, intent) will result in a cache miss.
     */
    fun generateFingerprint(
        promptVersion: String,
        modelVersion: String,
        retrievalPolicyVersion: String,
        appearanceTelemetry: String,
        weatherState: String,
        userIntent: String,
        minifiedManifest: String
    ): String {
        val input = StringBuilder().apply {
            append(promptVersion)
            append(modelVersion)
            append(retrievalPolicyVersion)
            append(appearanceTelemetry)
            append(weatherState)
            append(userIntent)
            append(minifiedManifest)
        }.toString()
        
        return hashString(input)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Returns the current number of items in the cache.
     */
    fun size(): Int = cache.size()
}
