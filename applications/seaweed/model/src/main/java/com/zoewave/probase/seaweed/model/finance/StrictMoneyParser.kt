package com.zoewave.probase.seaweed.model.finance

import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

@Serializable
data class CurrencyAmount(
    val amountMinor: Long,
    val currencyCode: String,
    val currencyScale: Int
) {
    init {
        val expectedScale = Iso4217CurrencyRegistry.getScaleOrNull(currencyCode)
        require(expectedScale != null) { "Unsupported ISO 4217 code: $currencyCode" }
        require(currencyScale == expectedScale) {
            "Scale mismatch for $currencyCode: expected $expectedScale, got $currencyScale"
        }
    }

    fun toBigDecimal(): BigDecimal =
        BigDecimal.valueOf(amountMinor, currencyScale)
}

sealed class MoneyParseResult {
    data class Exact(val amount: CurrencyAmount) : MoneyParseResult()
    data class ExcessPrecisionFlagged(
        val rawToken: String,
        val currencyCode: String,
        val observedScale: Int,
        val expectedScale: Int
    ) : MoneyParseResult()
    data class UnknownCurrencyCode(val currencyCode: String) : MoneyParseResult()
    data class MalformedNumber(val rawToken: String) : MoneyParseResult()
}

object StrictMoneyParser {
    fun parse(rawDecimalToken: String, currencyCode: String, docId: String = "GLOBAL"): MoneyParseResult {
        val normalizedCode = currencyCode.trim().uppercase()
        val expectedScale = Iso4217CurrencyRegistry.getScaleOrNull(normalizedCode)
            ?: run {
                val res = MoneyParseResult.UnknownCurrencyCode(normalizedCode)
                PipelineGearLogger.d(PipelineGear.GEAR_4_MONEY_EXTRACT, docId) { "Parse UnknownCurrencyCode: $normalizedCode" }
                return res
            }

        val cleaned = rawDecimalToken.trim().replace(" ", "")
        val bd = cleaned.toBigDecimalOrNull()?.stripTrailingZeros()
            ?: run {
                val res = MoneyParseResult.MalformedNumber(rawDecimalToken)
                PipelineGearLogger.d(PipelineGear.GEAR_4_MONEY_EXTRACT, docId) { "Parse MalformedNumber: $rawDecimalToken" }
                return res
            }

        val observedScale = bd.scale().coerceAtLeast(0)
        if (observedScale > expectedScale) {
            val res = MoneyParseResult.ExcessPrecisionFlagged(
                rawToken = rawDecimalToken,
                currencyCode = normalizedCode,
                observedScale = observedScale,
                expectedScale = expectedScale
            )
            PipelineGearLogger.d(PipelineGear.GEAR_4_MONEY_EXTRACT, docId) { "Parsed ExcessPrecisionFlagged: $rawDecimalToken ($observedScale > $expectedScale)" }
            return res
        }

        return try {
            val minor = bd.setScale(expectedScale, RoundingMode.UNNECESSARY)
                .movePointRight(expectedScale)
                .longValueExact()
            val amount = CurrencyAmount(
                amountMinor = minor,
                currencyCode = normalizedCode,
                currencyScale = expectedScale
            )
            PipelineGearLogger.d(PipelineGear.GEAR_4_MONEY_EXTRACT, docId) { "Parsed Exact: $minor $normalizedCode (scale=$expectedScale) from $rawDecimalToken" }
            MoneyParseResult.Exact(amount)
        } catch (e: ArithmeticException) {
            PipelineGearLogger.d(PipelineGear.GEAR_4_MONEY_EXTRACT, docId) { "Parse MalformedNumber (ArithmeticException): $rawDecimalToken" }
            MoneyParseResult.MalformedNumber(rawDecimalToken)
        }
    }
}
