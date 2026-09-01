# FASHIONISTA Engine vs. KoColor Recommendation Pipeline Architecture

This document defines the strict architectural boundary between **FASHIONISTA** (the standalone, 100% offline, zero-network deterministic aesthetic scoring calculator) and the **KoColor Recommendation Engine** (the AI-assisted style architect for daily outfit synthesis).

---

## 1. Architectural Separation

| Dimension | KoColor Recommendation Engine | FASHIONISTA Scoring Engine |
| :--- | :--- | :--- |
| **Primary Question** | *"What should I wear today?"* | *"How good is this complete observed ensemble?"* |
| **Input** | Context Stream (Weather, Occasion, Wardrobe Inventory, User Intent) | Observed Outfit (Flat-lay, Photo, or Selected Garments + Cosmetics) |
| **Network & AI** | Optional LLM (Firebase Vertex AI / Gemini / Local AI) | **ZERO Network, ZERO LLM, 100% Offline Math** |
| **Candidate Retrieval**| Candidate Search, Filtering & Ranking | **NO Retrieval, NO Wardrobe Search** |
| **Output** | Selected Outfit + Editorial AI Rationale | `FashionistaResult` (`0–100` Score + `Coverage` + 6-Axis Radar) |
| **Provenance** | Internal Wardrobe Inventory Only | **Provenance Agnostic** (Wardrobe, Camera, Pinterest, Flat-lay) |

---

## 2. Hard Boundary Flow

```text
                                KoColor System
                                      │
            ┌─────────────────────────┴─────────────────────────┐
            │                                                   │
     RECOMMENDATION                                        FASHIONISTA
  "What should I wear today?"                         "How good is this?"
            │                                                   │
    • Weather & Occasion                                • NO Weather / Occasion
    • Wardrobe Search & Retrieval                       • NO Wardrobe Search / Retrieval
    • Candidate Ranking & Filtering                     • NO Candidate Ranking
    • LLM Prompt & Rationale Synthesis                  • 100% Deterministic $L^*C^*h^\circ$ Math
            │                                           • ZERO Network / ZERO LLM
            ▼                                                   ▼
    Selected Outfit + Rationale                         FashionistaResult (0–100 + Radar)
```

---

## 3. Engine Return Contract & Data Types

Instead of returning a primitive `Float`, the FASHIONISTA engine emits a complete snapshot that Jetpack Compose UI state flows can observe directly:

```kotlin
/**
 * Immutable snapshot emitted by the offline FASHIONISTA Engine.
 */
data class FashionistaResult(
    val absoluteScore: Float, // 0.0 to 100.0
    val coverage: Float,      // 0.0 to 1.0 (portion of visual system observed)
    val radarBreakdown: RadarMetrics
)

/**
 * 6-axis aesthetic diagnostic breakdown.
 */
data class RadarMetrics(
    val composition: Float,
    val colorHarmony: Float,
    val silhouette: Float,
    val textureHarmony: Float,
    val visualHierarchy: Float,
    val wearerIntegration: Float
)
```

---

## 4. Step-by-Step Implementation Guide to Build the Separation

### Step 1: Isolate FASHIONISTA Calculations from AI Synthesis Loops
In `StyleSimulatorViewModel.kt`, ensure color telemetry edits (`OnManualSkinColorSelected`, `OnManualEyeColorSelected`, `OnManualHairColorSelected`) execute **only** the offline `ColorHarmonyEngine` / `seasonClassifier.classify()` logic.

Do **NOT** trigger `debouncedRunSimulation()` or Gemini network requests when updating telemetry swatches.

### Step 2: Refined ViewModel State Flow Implementation
When calibrating telemetry swatches, compute the `FashionistaResult` offline and emit it directly to the UI layer without network execution:

```kotlin
// In StyleSimulatorViewModel.kt:
private fun updateManualTelemetry(
    skinHex: String? = null,
    eyeHex: String? = null,
    hairHex: String? = null
) {
    val current = _faceTelemetry.value ?: FaceTelemetryData(...)
    val updatedTelemetry = current.copy(...)

    _faceTelemetry.value = updatedTelemetry

    // 1. Pure Offline FASHIONISTA Score & Radar Calculation
    val vector = FacialContrastVector(
        updatedTelemetry.skinLuminance,
        updatedTelemetry.hairLuminance,
        updatedTelemetry.eyeLuminance,
        updatedTelemetry.contrastDelta
    )
    val season = seasonClassifier.classify(vector, updatedTelemetry.undertoneScore)

    // Emits full FashionistaResult (absoluteScore, coverage, 6-axis radar metrics)
    val fashionistaResult = colorHarmonyEngine.calculateFashionistaResult(
        finalOutfit = _recommendedClothing.value,
        finalCosmetics = _recommendedCosmetics.value,
        telemetry = ColorTelemetry(
            undertoneScore = updatedTelemetry.undertoneScore,
            contrastDelta = updatedTelemetry.contrastDelta,
            skinLuminance = updatedTelemetry.skinLuminance
        )
    )

    // 2. Update UI State Directly (Zero Network)
    _fashionistaMetrics.value = fashionistaResult

    val profile = ColorProfile(
        season = season,
        undertone = updatedTelemetry.undertoneScore,
        contrastVector = vector,
        optimalPaletteHexCodes = seasonClassifier.getOptimalPalette(season)
    )

    viewModelScope.launch {
        fashionRepository.saveProfile(profile.toFashionProfile())
        Log.d("StyleSimulatorVM", "FASHIONISTA Offline Result: Score=${fashionistaResult.absoluteScore}, Season=$season")
    }
}
```

### Step 3: Explicit User Trigger for AI Recommendations
Keep Gemini AI synthesis (`debouncedRunSimulation()`) mapped exclusively to the explicit user action button: **"Generate AI Outfit Recommendation"** / **"Start AI Synthesis"**.

---

## 5. Mathematical Precision & $L^*C^*h^\circ$ Guarantee

Locking FASHIONISTA strictly to geometric contrast vectors and $L^*C^*h^\circ$ mathematical color harmony guarantees that an outfit photo captured on the street, pulled from Pinterest, or built from the local closet is evaluated with 100% identical, immutable, offline precision.

---

## 6. Verification Checklist

- [x] **Zero Network Requests on Telemetry Edits**: Tapping skin/eye/hair swatches does not trigger Firebase/Gemini API calls.
- [x] **State Flow Contract (`FashionistaResult`)**: Emits `absoluteScore`, `coverage`, and `radarBreakdown` directly to Jetpack Compose UI state.
- [x] **Instant Quadrant & Radar Refresh**: Color changes instantly update the Seasonal Quadrant Map, target dot, raw telemetry, and aesthetic profile.
- [x] **Coverage Metric Separation**: Missing face data reduces coverage percentage, not the intrinsic ensemble score.
- [x] **Provenance Agnosticism**: FASHIONISTA accepts any 3-piece or flat-lay ensemble from any source.
