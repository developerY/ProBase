package com.zoewave.probase.seaweed.model.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrictMoneyParserTest {

    @Test
    fun `parse JPY 1500 returns amountMinor 1500, scale 0`() {
        val result = StrictMoneyParser.parse("1500", "JPY")
        assertTrue(result is MoneyParseResult.Exact)
        val amount = (result as MoneyParseResult.Exact).amount
        assertEquals(1500L, amount.amountMinor)
        assertEquals(0, amount.currencyScale)
    }

    @Test
    fun `parse USD 20_07 returns amountMinor 2007, scale 2`() {
        val result = StrictMoneyParser.parse("20.07", "USD")
        assertTrue(result is MoneyParseResult.Exact)
        val amount = (result as MoneyParseResult.Exact).amount
        assertEquals(2007L, amount.amountMinor)
        assertEquals(2, amount.currencyScale)
    }

    @Test
    fun `parse KWD 4_125 returns amountMinor 4125, scale 3`() {
        val result = StrictMoneyParser.parse("4.125", "KWD")
        assertTrue(result is MoneyParseResult.Exact)
        val amount = (result as MoneyParseResult.Exact).amount
        assertEquals(4125L, amount.amountMinor)
        assertEquals(3, amount.currencyScale)
    }

    @Test
    fun `parse PLN 49_99 returns amountMinor 4999, scale 2`() {
        val result = StrictMoneyParser.parse("49.99", "PLN")
        assertTrue(result is MoneyParseResult.Exact)
        val amount = (result as MoneyParseResult.Exact).amount
        assertEquals(4999L, amount.amountMinor)
        assertEquals(2, amount.currencyScale)
    }

    @Test
    fun `parse 12_345 for USD returns ExcessPrecisionFlagged and never rounds to 1235`() {
        val result = StrictMoneyParser.parse("12.345", "USD")
        assertTrue(result is MoneyParseResult.ExcessPrecisionFlagged)
        val error = result as MoneyParseResult.ExcessPrecisionFlagged
        assertEquals("12.345", error.rawToken)
        assertEquals(3, error.observedScale)
        assertEquals(2, error.expectedScale)
    }
}
