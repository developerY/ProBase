package com.zoewave.probase.kocolor.fashionista.domain

/**
 * Synchronous, context-free computational evaluator contract.
 * Designed to be thread-safe for background worker execution.
 */
interface FashionistaScorer {
    fun score(outfit: FashionistaObservation): FashionistaScore
}
