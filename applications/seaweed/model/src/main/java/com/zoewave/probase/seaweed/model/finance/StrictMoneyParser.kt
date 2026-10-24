package com.zoewave.probase.seaweed.model.finance

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
    fun parse(rawDecimalToken: String, currencyCode: String): MoneyParseResult {
        val normalizedCode = currencyCode.trim().uppercase()
        val expectedScale = Iso4217CurrencyRegistry.getScaleOrNull(normalizedCode)
            ?: return MoneyParseResult.UnknownCurrencyCode(normalizedCode)

        val cleaned = rawDecimalToken.trim().replace(" ", "")
        val bd = cleaned.toBigDecimalOrNull()?.stripTrailingZeros()
            ?: return MoneyParseResult.MalformedNumber(rawDecimalToken)

        val observedScale = bd.scale().coerceAtLeast(0)
        if (observedScale > expectedScale) {
            return MoneyParseResult.ExcessPrecisionFlagged(
                rawToken = rawDecimalToken,
                currencyCode = normalizedCode,
                observedScale = observedScale,
                expectedScale = expectedScale
            )
        }

        return try {
            val minor = bd.setScale(expectedScale, RoundingMode.UNNECESSARY)
                .movePointRight(expectedScale)
                .longValueExact()
            MoneyParseResult.Exact(
                CurrencyAmount(
                    amountMinor = minor,
                    currencyCode = normalizedCode,
                    currencyScale = expectedScale
                )
            )
        } catch (e: ArithmeticException) {
            MoneyParseResult.MalformedNumber(rawDecimalToken)
        }
    }
}
