package com.zoewave.probase.seaweed.model.finance.common

import android.util.Log

enum class PipelineGear(val prefix: String) {
    GEAR_1_CAPTURE_CRYPTO("⚙️ [GEAR-1:CAPTURE-CRYPTO]"),
    GEAR_2_VISION_HOMOGRAPHY("📐 [GEAR-2:VISION-HOMOGRAPHY]"),
    GEAR_3_LAYOUT_OCR("🧱 [GEAR-3:LAYOUT-OCR]"),
    GEAR_4_MONEY_EXTRACT("💱 [GEAR-4:MONEY-EXTRACT]"),
    GEAR_5_LEDGER_GATE("🔒 [GEAR-5:LEDGER-GATE]"),
    GEAR_6_REDACT_TRIM("🛡️ [GEAR-6:REDACT-TRIM]"),
    GEAR_7_AI_VALIDATOR("🧠 [GEAR-7:AI-VALIDATOR]"),
    GEAR_8_DB_ENVELOPE("🏦 [GEAR-8:DB-ENVELOPE]")
}

object PipelineGearLogger {
    const val TAG = "SeaWeedEngine"

    @Volatile
    var isDebugEnabled: Boolean = true

    @Volatile
    var forceStdoutForTests: Boolean = false

    inline fun d(gear: PipelineGear, docId: String = "GLOBAL", message: () -> String) {
        if (!isDebugEnabled) return
        val formatted = "${gear.prefix} [doc=$docId] ${message()}"
        if (forceStdoutForTests) {
            println("D/$TAG: $formatted")
            return
        }
        try {
            val written = Log.d(TAG, formatted)
            if (written <= 0) println("D/$TAG: $formatted")
        } catch (_: Throwable) {
            println("D/$TAG: $formatted")
        }
    }
}
