package com.zoewave.probase.features.ai.local.data

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class PromptCacheRepositoryTest {

    private lateinit var cache: PromptCacheRepository

    @Before
    fun setUp() {
        cache = PromptCacheRepository()
    }

    @Test
    fun generateFingerprint_boundaryShiftedInputs_producesDistinctKeys() {
        val key1 = cache.generateFingerprint(
            executionTier = "A",
            promptVersion = "BC",
            modelVersion = "m1",
            retrievalPolicyVersion = "r1",
            appearanceTelemetry = "telemetry",
            weatherState = "sunny",
            userIntent = "work",
            minifiedManifest = "manifest"
        )

        val key2 = cache.generateFingerprint(
            executionTier = "AB",
            promptVersion = "C",
            modelVersion = "m1",
            retrievalPolicyVersion = "r1",
            appearanceTelemetry = "telemetry",
            weatherState = "sunny",
            userIntent = "work",
            minifiedManifest = "manifest"
        )

        assertThat(key1).isNotEqualTo(key2)
    }

    @Test
    fun putAndGet_identicalFingerprint_returnsCachedValue() {
        val fp = cache.generateFingerprint(
            executionTier = "tier1",
            promptVersion = "v1",
            modelVersion = "m1",
            retrievalPolicyVersion = "r1",
            appearanceTelemetry = "telemetry",
            weatherState = "sunny",
            userIntent = "work",
            minifiedManifest = "manifest"
        )

        cache.put(fp, "cached_result")
        assertThat(cache.get(fp)).isEqualTo("cached_result")
    }
}
