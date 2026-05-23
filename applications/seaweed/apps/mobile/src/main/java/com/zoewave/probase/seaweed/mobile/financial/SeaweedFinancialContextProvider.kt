package com.zoewave.probase.seaweed.mobile.financial

import com.zoewave.probase.core.model.FinancialContextProvider
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.EnvelopeRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject

class SeaweedFinancialContextProvider @Inject constructor(
    private val envelopeRepo: EnvelopeRepository
) : FinancialContextProvider {

    override suspend fun getFinancialContext(): String? {
        val envelopes = envelopeRepo.getAllEnvelopes().firstOrNull() ?: return null
        
        return buildJsonObject {
            put("currency", "USD")
            putJsonObject("envelopes") {
                envelopes.forEach { envelope ->
                    put(envelope.name.lowercase(), (envelope.monthlyLimitCents - envelope.currentSpentCents) / 100.0)
                }
            }
        }.toString()
    }
}
