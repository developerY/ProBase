package com.zoewave.probase.gotmind.features.mindwave

import kotlinx.coroutines.CoroutineScope

class SymphonyMindWaveEngine(
    scope: CoroutineScope,
    onGameOver: (Int, Int) -> Unit
) : BaseMindWaveEngine(scope, onGameOver) {

    private val pastelColors = listOf(
        // Row 0: High Notes (Red/Orange Pastels)
        0xFFFFB7B2L, 0xFFFFC8B2L, 0xFFFFDAC1L, 0xFFFFE5C1L,
        // Row 1: Mid-High (Yellow/Green Pastels)
        0xFFFFF9C4L, 0xFFF1F8E9L, 0xFFE2F0CBL, 0xFFD4E157L,
        // Row 2: Mid-Low (Blue/Teal Pastels)
        0xFFB5EAD7L, 0xFFB2EBF2L, 0xFF81D4FAL, 0xFFC5CAE9L,
        // Row 3: Low Notes (Purple/Pink Pastels)
        0xFFD1C4E9L, 0xFFE1BEE7L, 0xFFF3E5F5L, 0xFFF8BBD0L
    )

    private val musicNotes = listOf(
        "D#5", "D5", "C#5", "C5",  // Row 0 (Top)
        "B4", "A#4", "A4", "G#4",  // Row 1
        "G4", "F#4", "F4", "E4",   // Row 2
        "D#4", "D4", "C#4", "C4"   // Row 3 (Bottom)
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
