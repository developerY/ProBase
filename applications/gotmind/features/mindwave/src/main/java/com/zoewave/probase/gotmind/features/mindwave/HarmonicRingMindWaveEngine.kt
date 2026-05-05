package com.zoewave.probase.gotmind.features.mindwave

import kotlinx.coroutines.CoroutineScope

class HarmonicRingMindWaveEngine(
    scope: CoroutineScope,
    onGameOver: (Int, Int) -> Unit
) : BaseMindWaveEngine(com.zoewave.probase.gotmind.model.MindWaveMode.HARMONIC_RING, scope, onGameOver) {

    private val pastelColors = listOf(
        0xFFFFB7B2L, 0xFFFFC8B2L, 0xFFFFDAC1L, 0xFFFFE5C1L,
        0xFFFFF9C4L, 0xFFF1F8E9L, 0xFFE2F0CBL, 0xFFD4E157L,
        0xFFB5EAD7L, 0xFFB2EBF2L, 0xFF81D4FAL, 0xFFC5CAE9L,
        0xFFD1C4E9L, 0xFFE1BEE7L, 0xFFF3E5F5L, 0xFFF8BBD0L
    )

    private val musicNotes = listOf(
        "D#5", "D5", "C#5", "C5",
        "B4", "A#4", "A4", "G#4",
        "G4", "F#4", "F4", "E4",
        "D#4", "D4", "C#4", "C4"
    )

    override fun createInitialGrid(): List<Node> {
        return List(16) { i ->
            Node(
                id = i,
                color = pastelColors[i % pastelColors.size],
                note = musicNotes[i % musicNotes.size]
            )
        }
    }
}
