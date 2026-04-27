package com.zoewave.probase.seaweed.features.spendingcontrol.data

import com.zoewave.probase.seaweed.features.spendingcontrol.domain.Envelope
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.EnvelopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryEnvelopeRepository @Inject constructor() : EnvelopeRepository {

    private val envelopes = MutableStateFlow<Map<String, Envelope>>(emptyMap())

    override fun getAllEnvelopes(): Flow<List<Envelope>> = 
        envelopes.map { it.values.toList() }

    override suspend fun getEnvelopeForCategory(categoryId: String): Envelope? {
        return envelopes.value.values.find { it.categoryIds.contains(categoryId) }
    }

    override suspend fun updateSpentAmount(envelopeId: String, amountCents: Long) {
        val current = envelopes.value[envelopeId] ?: return
        envelopes.value += (envelopeId to current.copy(
            currentSpentCents = current.currentSpentCents + amountCents
        ))
    }

    override suspend fun saveEnvelope(envelope: Envelope) {
        envelopes.value += (envelope.id to envelope)
    }
}
