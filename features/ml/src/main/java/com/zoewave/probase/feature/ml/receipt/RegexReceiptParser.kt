package com.zoewave.probase.feature.ml.receipt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RegexReceiptParser {

    private val amountRegex = Regex("""\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{2})""")
    private val dateRegex = Regex("""(\d{1,4}[-/.]\d{1,2}[-/.]\d{1,4})""")

    suspend fun parse(text: String): ReceiptResult = withContext(Dispatchers.Default) {
        val amounts = amountRegex.findAll(text)
            .mapNotNull { it.value.replace(",", ".").toDoubleOrNull() }
            .toList()
        
        val totalAmount = amounts.maxOrNull() ?: 0.0
        
        val date = dateRegex.find(text)?.value ?: ""
        
        ReceiptResult(
            totalAmount = totalAmount,
            date = date
        )
    }
}
