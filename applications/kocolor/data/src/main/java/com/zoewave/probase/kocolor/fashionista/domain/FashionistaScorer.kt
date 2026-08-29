package com.zoewave.probase.kocolor.fashionista.domain

/**
 * Context-free computational evaluator contract.
 * Suspending and thread-safe for off-UI-thread execution (Dispatchers.Default).
 */
interface FashionistaScorer {
    suspend fun score(outfit: FashionistaObservation): FashionistaScore
}
