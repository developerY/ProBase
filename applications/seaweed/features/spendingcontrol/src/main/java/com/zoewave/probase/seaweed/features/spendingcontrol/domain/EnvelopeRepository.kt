package com.zoewave.probase.seaweed.features.spendingcontrol.domain

import kotlinx.coroutines.flow.Flow

interface EnvelopeRepository {
    fun getAllEnvelopes(): Flow<List<Envelope>>
    suspend fun getEnvelopeForCategory(categoryId: String): Envelope?
    suspend fun updateSpentAmount(envelopeId: String, amountCents: Long)
    suspend fun saveEnvelope(envelope: Envelope)
}
