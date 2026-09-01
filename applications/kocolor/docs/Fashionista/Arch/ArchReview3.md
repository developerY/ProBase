These final refinements lock the FASHIONISTA architecture into a mathematically rigorous, side-effect-free standard. The canonical specification below is ready for immediate implementation.

## FASHIONISTA Engine vs. KoColor Recommendation Pipeline Architecture

This document defines the strict architectural boundary between **FASHIONISTA** (the standalone, 100% offline, zero-network deterministic aesthetic evaluation engine) and the **KoColor Recommendation Engine** (the AI-assisted style architect for daily outfit synthesis).

### 1. Architectural Separation

| Dimension | KoColor Recommendation Engine | FASHIONISTA Scoring Engine |
| --- | --- | --- |
| **Primary Question** | *"What should I wear?"* | *"How good is this?"* |
| **Input** | Context Stream (Weather, Occasion, Wardrobe, User Intent) | `FashionistaObservation` (Pure visual properties) |
| **Network & AI** | Optional LLM (Firebase Vertex AI / Gemini / Local AI) | **ZERO Network, ZERO LLM, 100% Offline Deterministic Computation** |
| **Candidate Retrieval** | Candidate Search, Filtering & Ranking | **NO Retrieval, NO Wardrobe Search** |
| **Output** | Selected Outfit + Editorial AI Rationale | `FashionistaResult` (`aestheticScore`, `coverage`, 6-Axis Radar) |
| **Provenance** | Internal Wardrobe Inventory Only | **Provenance Agnostic** (Accepts arbitrary observed ensembles) |

### 2. Hard Boundary Flow

```text
                         KoColor
                            │
             ┌──────────────┴──────────────┐
             │                             │
             ▼                             ▼
      RECOMMENDATION                   FASHIONISTA
   "What should I wear?"            "How good is this?"
             │                             │
     Context / Weather              FashionistaObservation
     Occasion                              │
     User Intent                           │
     Wardrobe                         Deterministic
     Retrieval                        Evaluation
     Ranking                               │
     Gemini / LLM                          ▼
             │                       Frozen Calibration
             ▼                             │
       Selected Outfit                     ▼
       + Rationale                  FashionistaResult
                                     ├─ 0–100 Score
                                     ├─ Coverage
                                     ├─ 6-Axis Radar
                                     └─ Calibration Version

```

**Critical Dependency Rule:**

* `RECOMMENDATION` ───────X──────► `FASHIONISTA`
* `FASHIONISTA` ──────────X──────► `RECOMMENDATION`

*(Both engines may consume shared deterministic extraction libraries, but neither engine depends on the other).*

### 3. Engine Return Contract & Data Types

FASHIONISTA requires no knowledge of the application's underlying repository. It strictly forbids database IDs, `WardrobeItem` entity references, or application state within its input. It receives a pure visual observation and must **never mutate** the observation it receives.

```kotlin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Pure observation layer containing strictly visual properties.
 */
data class FashionistaObservation(
    val garments: ImmutableList<GarmentObservation>,
    val cosmetics: ImmutableList<CosmeticObservation>,
    val silhouetteMask: SilhouetteObservation? = null,
    val textureObservations: ImmutableList<TextureObservation> = persistentListOf(),
    val wearer: WearerObservation? = null
)

/**
 * Immutable, reference-calibrated snapshot emitted by the offline engine.
 * 
 * Invariants:
 * - aestheticScore ∈ [0, 100]
 * - coverage ∈ [0, 1]
 * - all radar metrics ∈ [0, 1]
 * - calibrationVersion identifies the exact scoring standard
 */
data class FashionistaResult(
    val aestheticScore: Float,       
    val coverage: Float,             
    val radarBreakdown: RadarMetrics,
    val calibrationVersion: String   
)

data class RadarMetrics(
    val composition: Float,
    val colorHarmony: Float,
    val silhouette: Float,
    val textureHarmony: Float,
    val visualHierarchy: Float,
    val wearerIntegration: Float
)

```

### 4. The Determinism Contract

FASHIONISTA operates under strict mathematical predictability.

`FashionistaObservation` + `FashionistaCalibration` = **`FashionistaResult`**

Given an identical observation and identical calibration version, FASHIONISTA produces the exact same deterministic result. To guarantee this across device architectures, the implementation strictly enforces:

* Fixed iteration order for all collection processing.
* No unordered parallel reductions.
* Deterministic floating-point operations (safely managed by dropping complex matrix math or GLCM spatial texture processing down to Rust or equivalent systems-level binaries if necessary).
* Deterministic image preprocessing and color conversion.
* Deterministic tie handling.
* Zero random seeds, random sampling, timestamps, or device-specific environmental inputs.

### 5. Score vs. Coverage Semantics

Missing physical data does not artificially degrade an outfit's aesthetic score.

* **Score:** The aesthetic quality of the available evidence.
* **Coverage:** The completeness of the available evidence.

If a flat-lay photograph is evaluated (no face detected), the wearer evidence is unavailable. Because the wearer contribution is excluded, the **Coverage** decreases, but the **Score** is not artificially penalized.

* **High Score + Low Coverage:** *"Looks excellent based on limited evidence."*
* **High Score + High Coverage:** *"Looks excellent based on comprehensive evidence."*

### 6. UI & State Flow Decoupling

FASHIONISTA does not own or modify user personal color profiles, seasonal palettes, or profile persistence. When the user manually edits telemetry (Skin/Eye/Hair) in the Jetpack Compose UI, the ViewModel routes state to the respective systems completely independently:

```kotlin
// In StyleSimulatorViewModel.kt:
private fun updateManualTelemetry(newTelemetry: FaceTelemetryData) {
    _faceTelemetry.value = newTelemetry

    // 1. Personal Color Engine (Handles identity & persistence)
    viewModelScope.launch {
        val season = personalColorEngine.classify(newTelemetry)
        val profile = ColorProfile(season, newTelemetry)
        fashionRepository.saveProfile(profile)
    }

    // 2. FASHIONISTA Engine (Evaluates the current visual state)
    val currentObservation = FashionistaObservation(
        garments = _currentGarmentObservations.value.toImmutableList(),
        cosmetics = _currentCosmeticObservations.value.toImmutableList(),
        silhouetteMask = currentMask,
        textureObservations = currentTextures.toImmutableList(),
        wearer = WearerObservation(newTelemetry) 
    )
    
    _fashionistaMetrics.value = fashionistaEngine.evaluate(
        observation = currentObservation,
        calibration = activeCalibrationStandard // e.g., v1.1
    )
}

```