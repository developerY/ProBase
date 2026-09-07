package com.zoewave.probase.features.ai.core

import android.graphics.Bitmap

/**
 * Sealed interface for type-safe AI input bifurcation.
 * Enforces compile-time privacy invariants: Cloud providers strictly accept [TextOnly],
 * while local on-device providers can accept [Multimodal].
 */
sealed interface AiInput {
    val promptString: String
    val temperatureOverride: Float?

    data class TextOnly(
        override val promptString: String,
        override val temperatureOverride: Float? = null
    ) : AiInput

    data class Multimodal(
        override val promptString: String,
        val localImage: Bitmap,
        override val temperatureOverride: Float? = null
    ) : AiInput
}
