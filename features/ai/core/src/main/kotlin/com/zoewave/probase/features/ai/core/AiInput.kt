package com.zoewave.probase.features.ai.core

import android.graphics.Bitmap

/**
 * Sealed interface for type-safe AI input bifurcation.
 * Enforces compile-time privacy invariants: Cloud providers strictly accept [TextOnly],
 * while local on-device providers can accept [Multimodal].
 */
sealed interface AiInput {
    val promptString: String

    data class TextOnly(
        override val promptString: String
    ) : AiInput

    data class Multimodal(
        override val promptString: String,
        val localImage: Bitmap
    ) : AiInput
}
