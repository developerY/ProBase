package com.zoewave.probase.kocolor.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordMatcherTest {

    @Test
    fun `tokenize normalizes plurals to single canonical stem`() {
        assertEquals(setOf("black", "boot"), KeywordMatcher.tokenize("black boots"))
        assertEquals(setOf("ankle", "boot"), KeywordMatcher.tokenize("Ankle Boot"))
        assertEquals(setOf("silk", "dress"), KeywordMatcher.tokenize("Silk Dresses"))
    }

    @Test
    fun `countMatchingTokens counts plural match exactly once without double counting`() {
        assertEquals(1, KeywordMatcher.countMatchingTokens("Ankle Boots", "boots"))
        assertEquals(1, KeywordMatcher.countMatchingTokens("Ankle Boot", "boots"))
        assertEquals(1, KeywordMatcher.countMatchingTokens("Ankle Boots", "boot"))
    }

    @Test
    fun `containsWholeWord matches singular and plural symmetrically`() {
        assertTrue(KeywordMatcher.containsWholeWord("Ankle Boot", "boots"))
        assertTrue(KeywordMatcher.containsWholeWord("Ankle Boots", "boot"))
        assertTrue(KeywordMatcher.containsWholeWord("Summer Dresses", "dress"))
    }

    @Test
    fun `containsWholeWord requires contiguous sequence for multiword phrases`() {
        assertTrue(KeywordMatcher.containsWholeWord("Tailored Red Dress", "red dress"))
        assertFalse(KeywordMatcher.containsWholeWord("Red Blouse with a Dress", "red dress"))
        assertEquals(2, KeywordMatcher.countMatchingTokens("Red Blouse with a Dress", "red dress"))
    }

    @Test
    fun `stop words are stripped and never match substrings`() {
        assertEquals(0, KeywordMatcher.countMatchingTokens("Alpine Jacket", "a for the in on"))
        assertFalse(KeywordMatcher.containsWholeWord("Alpine Jacket", "a"))
    }

    @Test
    fun `substrings inside larger words do not match`() {
        assertFalse(KeywordMatcher.containsWholeWord("Eclipse Eyeshadow", "lip"))
        assertFalse(KeywordMatcher.containsWholeWord("Stop Sign Red Tee", "top"))
        assertFalse(KeywordMatcher.containsWholeWord("Sublimated Muslin Shirt", "slim"))
        assertTrue(KeywordMatcher.containsWholeWord("Slim Fit Trousers", "slim"))
    }
}
