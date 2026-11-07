package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProviderCapability
import org.junit.Before
import org.junit.Test

class PromptAssemblerTest {

    private lateinit var promptAssembler: PromptAssembler

    @Before
    fun setUp() {
        promptAssembler = PromptAssembler()
    }

    @Test
    fun assembleCompactNano_scaffoldSize_isUnder180Tokens() {
        val context = StyleRequestContext(
            intent = "work",
            occasion = "Office",
            weatherTempC = 22f
        )
        val capability = AiProviderCapability(
            id = "gemini_nano",
            displayName = "Nano",
            maxInputTokens = 768,
            maxOutputTokens = 256,
            timeoutMillis = 1200L,
            isLocal = true
        )

        val result = promptAssembler.assembleCompactNano(
            context = context,
            wardrobeManifest = "w_1|TOP|Shirt|#000",
            cosmeticManifest = "c_1|Lip|Lipstick|#F00",
            missingRoles = setOf("SHOES"),
            providerCapability = capability
        )

        assertThat(result).isInstanceOf(AiInput.TextOnly::class.java)
        val promptText = result.promptString
        val charLength = promptText.length
        val estimatedTokens = (charLength + 3) / 4

        assertThat(charLength).isLessThan(720)
        assertThat(estimatedTokens).isLessThan(180)
        assertThat(promptText).contains("SKIN:")
        assertThat(promptText).contains("TEMP:")
        assertThat(promptText).contains("INTENT:work")
        assertThat(promptText).contains("rationale")
    }
}
