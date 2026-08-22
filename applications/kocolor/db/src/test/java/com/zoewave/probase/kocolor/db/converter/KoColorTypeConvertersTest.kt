package com.zoewave.probase.kocolor.db.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class KoColorTypeConvertersTest {

    private val converters = KoColorTypeConverters()

    @Test
    fun `toStringList handles empty JSON array correctly`() {
        val json = "[]"
        val result = converters.toStringList(json)
        assertNotNull(result)
        assertEquals(0, result?.size)
    }

    @Test
    fun `toStringList handles null and blank input`() {
        assertEquals(null, converters.toStringList(null))
        assertEquals(emptyList<String>(), converters.toStringList(""))
        assertEquals(emptyList<String>(), converters.toStringList("   "))
    }

    @Test
    fun `fromStringList produces valid JSON`() {
        val list = listOf("a", "b")
        val json = converters.fromStringList(list)
        assertEquals("[\"a\",\"b\"]", json)
    }
}
