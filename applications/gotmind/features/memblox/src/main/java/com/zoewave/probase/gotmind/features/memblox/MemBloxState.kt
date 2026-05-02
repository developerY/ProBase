package com.zoewave.probase.gotmind.features.memblox

import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import java.util.UUID

enum class PowerUpType(val labelResId: Int, val icon: String) {
    FREEZE(R.string.applications_gotmind_features_memblox_pu_freeze, "❄️"),
    REVEAL(R.string.applications_gotmind_features_memblox_pu_reveal, "👁️"),
    NUKE(R.string.applications_gotmind_features_memblox_pu_nuke, "☢️"),
    HINT(R.string.applications_gotmind_features_memblox_pu_hint, "💡"),
    EQUALIZER(R.string.applications_gotmind_features_memblox_pu_equalizer, "💎"),
    SLOW(R.string.applications_gotmind_features_memblox_pu_slow, "⏳"),
    TIDY(R.string.applications_gotmind_features_memblox_pu_tidy, "🧹"),
    AUTO_MATCH(R.string.applications_gotmind_features_memblox_pu_auto, "🤖"),
    SCAN(R.string.applications_gotmind_features_memblox_pu_scan, "🔍")
}

enum class HapticSignal { LIGHT, MEDIUM, HEAVY }

data class ConfettiBurst(
    val id: String = UUID.randomUUID().toString(),
    val col: Int,
    val row: Int
)

data class FloatingTextEffect(
    val id: String = UUID.randomUUID().toString(),
    val textResId: Int,
    val col: Int,
    val row: Int,
    val color: Int = 0xFFFFEB3B.toInt()
)

data class MatchGhost(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val col: Int,
    val row: Int
)

data class Shockwave(
    val id: String = UUID.randomUUID().toString(),
    val col: Int,
    val row: Int
)

data class ScorePopup(
    val id: String = UUID.randomUUID().toString(),
    val score: Int,
    val col: Int,
    val row: Int
)

data class MemBloxState(
    val grid: List<MemBloxBlock> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val flippedBlocks: List<MemBloxBlock> = emptyList(),
    val pairsMatched: Int = 0,
    val totalPairsSpawned: Int = 0,
    val targetPairs: Int = 50,
    val cols: Int = 12,
    val rows: Int = 20,
    val difficulty: MemBloxDifficulty = MemBloxDifficulty.EXPERT,
    val isStarted: Boolean = false,
    
    // Analytics & New Mechanics
    val combo: Int = 0,
    val multiplier: Float = 1.0f,
    val peakCombo: Int = 0,
    val totalClicks: Int = 0,
    val successfulMatches: Int = 0,
    val missedMatches: Int = 0,
    val matchAccuracy: Float = 0f,
    val powerUps: Map<PowerUpType, Int> = mapOf(
        PowerUpType.FREEZE to 2, 
        PowerUpType.REVEAL to 1, 
        PowerUpType.NUKE to 1, 
        PowerUpType.HINT to 2,
        PowerUpType.EQUALIZER to 0,
        PowerUpType.SLOW to 2,
        PowerUpType.TIDY to 1,
        PowerUpType.AUTO_MATCH to 1,
        PowerUpType.SCAN to 2
    ),
    val powerUpsUsed: Int = 0,
    val isFrozen: Boolean = false,
    val isRevealed: Boolean = false,
    val isSlowed: Boolean = false,
    val isFrenzy: Boolean = false,
    val nukingBlockIds: Map<String, Int> = emptyMap(),
    val initiallyRevealedBlockIds: Set<String> = emptySet(),
    val confettiBursts: List<ConfettiBurst> = emptyList(),
    val activeShockwaves: List<Shockwave> = emptyList(),
    val floatingScores: List<ScorePopup> = emptyList(),
    
    // 6-Star Polish VFX State
    val shakeIntensity: Float = 0f,
    val frostAlpha: Float = 0f,
    val hintedBlockIds: Set<String> = emptySet(),
    val floatingTexts: List<FloatingTextEffect> = emptyList(),
    val matchGhosts: List<MatchGhost> = emptyList(),
    val lastHapticSignal: HapticSignal? = null,
    val isStressed: Boolean = false,
    
    // Skill Tracking
    val bestMatchStreak: Int = 0,
    val currentMatchStreak: Int = 0,
    val avgMatchTimeMs: Long = 0,
    val totalMatchTimeMs: Long = 0,
    val peakBoardBlocks: Int = 0,
    val firstFlipTimestamp: Long = 0,
    val finalRank: String = "",
    val isPaused: Boolean = false,
    val speedMultiplier: Float = 1.0f,
    val dropHeight: Int = 5,
    val dropDurationMillis: Int = 5000
)
