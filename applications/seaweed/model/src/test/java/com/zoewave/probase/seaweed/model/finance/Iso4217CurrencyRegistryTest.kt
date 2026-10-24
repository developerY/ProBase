package com.zoewave.probase.seaweed.model.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Iso4217CurrencyRegistryTest {

    @Test
    fun `registry resolves 0, 2, 3, and 4 decimal currencies correctly`() {
        assertEquals(0, Iso4217CurrencyRegistry.getScaleOrNull("JPY"))
        assertEquals(2, Iso4217CurrencyRegistry.getScaleOrNull("USD"))
        assertEquals(2, Iso4217CurrencyRegistry.getScaleOrNull("PLN"))
        assertEquals(2, Iso4217CurrencyRegistry.getScaleOrNull("ZAR"))
        assertEquals(3, Iso4217CurrencyRegistry.getScaleOrNull("KWD"))
        assertEquals(4, Iso4217CurrencyRegistry.getScaleOrNull("CLF"))
    }

    @Test
    fun `registry rejects non-monetary or unknown codes`() {
        assertNull(Iso4217CurrencyRegistry.getScaleOrNull("XAU"))
        assertNull(Iso4217CurrencyRegistry.getScaleOrNull("XXX"))
    }
}
