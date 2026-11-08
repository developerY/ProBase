package com.zoewave.probase.features.ai.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class JsonSanitizerTest {

    @Test
    fun extractFirstJsonObject_fencedJson_extractsCleanJson() {
        val raw = """
            Here is the response:
            ```json
            {
              "selectedClothingIds": ["w_1"],
              "rationale": "Looks great"
            }
            ```
            Hope that helps!
        """.trimIndent()

        val json = JsonSanitizer.extractFirstJsonObject(raw)
        assertThat(json).isNotNull()
        assertThat(json).contains("\"w_1\"")
        assertThat(json).contains("Looks great")
    }

    @Test
    fun extractFirstJsonObject_nestedObjects_extractsFullOuterObject() {
        val raw = """{"a":{"b":1}}"""
        val json = JsonSanitizer.extractFirstJsonObject(raw)
        assertThat(json).isEqualTo("""{"a":{"b":1}}""")
    }

    @Test
    fun extractFirstJsonObject_bracesInsideStringLiteral_preservesStringBraces() {
        val raw = """{"rationale": "Pairs {\"key\"} with {slot} nicely"}"""
        val json = JsonSanitizer.extractFirstJsonObject(raw)
        assertThat(json).isEqualTo("""{"rationale": "Pairs {\"key\"} with {slot} nicely"}""")
    }

    @Test
    fun extractFirstJsonObject_unbalancedTruncatedJson_returnsNull() {
        val raw = """{"selectedClothingIds": ["w_1""""
        val json = JsonSanitizer.extractFirstJsonObject(raw)
        assertThat(json).isNull()
    }
}
