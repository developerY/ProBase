package com.zoewave.probase.gotmind.features.mindwave

import androidx.annotation.StringRes

data class Melody(
    @StringRes val titleResId: Int,
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
            titleResId = R.string.applications_gotmind_features_mindwave_melody_ode_to_joy,
            sequence = listOf(11, 11, 10, 8, 8, 10, 11, 12)
        ),
        Melody(
            titleResId = R.string.applications_gotmind_features_mindwave_melody_major_arpeggio,
            sequence = listOf(15, 11, 8, 3)
        ),
        Melody(
            titleResId = R.string.applications_gotmind_features_mindwave_melody_star_path,
            sequence = listOf(15, 15, 8, 8, 6, 6, 8)
        ),
        Melody(
            titleResId = R.string.applications_gotmind_features_mindwave_melody_chromatic_wave,
            sequence = listOf(15, 14, 13, 12, 11, 10, 9, 8)
        ),
        Melody(
            titleResId = R.string.applications_gotmind_features_mindwave_melody_midnight_call,
            sequence = listOf(4, 6, 4, 11, 15)
        ),
        Melody(
            titleResId = R.string.applications_gotmind_features_mindwave_melody_high_peak,
            sequence = listOf(15, 12, 8, 4, 1, 0)
        )
    )

    fun getForLevel(level: Int): Melody {
        return melodies[(level - 1) % melodies.size]
    }
}
