package com.zoewave.ashbike.mobile.glass.ui

import org.junit.Test
import org.junit.Assert.assertEquals

class GlassViewModelTest {

    @Test
    fun `glass ui event select gear instantiation`() {
        val event = GlassUiEvent.SelectGear(5)
        assertEquals(5, event.gear)
    }

    @Test
    fun `glass ui event change screen instantiation`() {
        val event = GlassUiEvent.ChangeScreen(ScreenState.GEAR_LIST)
        assertEquals(ScreenState.GEAR_LIST, event.screen)
    }
}
