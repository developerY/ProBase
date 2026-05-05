package com.zoewave.probase.gotmind.features.mindwave

data class Melody(
    val title: String,
    val sequence: List<Int> // Node IDs 0..15
)

object MelodyLibrary {
    // 4x4 Grid Mapping (for reference):
    // Row 0: 0,  1,  2,  3   (High D#5..C5)
    // Row 1: 4,  5,  6,  7   (B4..G#4)
    // Row 2: 8,  9,  10, 11  (G4..E4)
    // Row 3: 12, 13, 14, 15  (D#4..C4)

    val melodies = listOf(
        Melody(
            title = "Ode to Joy (Snippet)",
            sequence = listOf(11, 11, 10, 8, 8, 10, 11, 12)
        ),
        Melody(
            title = "Major Arpeggio",
            sequence = listOf(15, 11, 8, 3)
        ),
        Melody(
            title = "Star Path",
            sequence = listOf(15, 15, 8, 8, 6, 6, 8)
        ),
        Melody(
            title = "Chromatic Wave",
            sequence = listOf(15, 14, 13, 12, 11, 10, 9, 8)
        ),
        Melody(
            title = "Midnight Call",
            sequence = listOf(4, 6, 4, 11, 15)
        ),
        Melody(
            title = "High Peak",
            sequence = listOf(15, 12, 8, 4, 1, 0)
        )
    )

    fun getForLevel(level: Int): Melody {
        return melodies[(level - 1) % melodies.size]
    }
}
