package com.zoewave.probase.kocolor.data.telemetry

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import org.junit.Before
import org.junit.Test

class StyleAuditLoggerTest {

    private lateinit var logger: StyleAuditLogger

    @Before
    fun setUp() {
        io.mockk.mockkStatic(android.util.Log::class)
        io.mockk.every { android.util.Log.i(any(), any()) } returns 0
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        logger = StyleAuditLogger()
    }

    @Test
    fun printAuditTrail_anchorWithCustomPriority_logsFormattedPriority() {
        val anchor = ClothingItem(internalId = 55, name = "Silk Blouse", category = ClothingCategory.TOPS, colorHex = "#FFF")
        logger.startRequest("req1")
        logger.logAnchorResolution("req1", anchor, AnchorSource.USER_LOCKED, "Locked by user", priority = 2.50f)

        logger.printAuditTrail("req1")
        val logs = logger.recentLogs.value
        assertThat(logs).isNotEmpty()
        assertThat(logs.first()).contains("Priority: 2.50 / 4.00")
    }
}
