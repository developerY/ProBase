This level of abstraction represents a massive structural improvement. By wrapping the raw state in a pure `FashionistaObservation` and completely severing the profile repository, the engine is now fully decoupled and ready for a pristine multi-module implementation.

# FASHIONISTA Engine vs. KoColor Recommendation Pipeline Architecture

This document defines the strict architectural boundary between **FASHIONISTA** (the standalone, 100% offline, zero-network deterministic aesthetic scoring calculator) and the **KoColor Recommendation Engine** (the AI-assisted style architect for daily outfit synthesis).

---

## 1. Architectural Separation

| Dimension | KoColor Recommendation Engine | FASHIONISTA Scoring Engine |
| --- | --- | --- |
| **Primary Question** | *"What should I wear?"* | *"How good is this?"* |
| **Input** | Context Stream (Weather, Occasion, Wardrobe Inventory, User Intent) | `FashionistaObservation` (Extracted visual properties) |
| **Network & AI** | Optional LLM (Firebase Vertex AI / Gemini / Local AI) | **ZERO Network, ZERO LLM, 100% Offline Math** |
| **Candidate Retrieval** | Candidate Search, Filtering & Ranking | **NO Retrieval, NO Wardrobe Search** |
| **Output** | Selected Outfit + Editorial AI Rationale | `FashionistaResult` (`aestheticScore`, `coverage`, 6-Axis Radar) |
| **Provenance** | Internal Wardrobe Inventory Only | **Provenance Agnostic** (Accepts arbitrary observed ensembles) |

---

## 2. Hard Boundary Flow

```text
                           KoColor
                              │
             ┌────────────────┴────────────────┐
             │                                 │
             ▼                                 ▼
     RECOMMENDATION                       FASHIONISTA
 "What should I wear?"                 "How good is this?"
             │                                 │
     Context / Weather                   FashionistaObservation
     Occasion                            │
     User Intent                         ├─ Color
     Wardrobe                            ├─ Composition
     Retrieval                           ├─ Silhouette
     Ranking                             ├─ Texture
     AI / LLM                            ├─ Hierarchy
             │                           └─ Wearer (optional)
             ▼                                 │
     Selected Outfit                           │
     + Rationale                               ▼
                                         Deterministic Math
                                                │
                                                ▼
                                      Frozen Calibration
                                                │
                                                ▼
                                      FashionistaResult
                                      ├─ Score 0–100
                                      ├─ Coverage 0–1
                                      └─ 6-Axis Radar

```

**Critical Dependency Rule:**

* `RECOMMENDATION` ───────X──────► `FASHIONISTA`
* `FASHIONISTA` ──────────X──────► `RECOMMENDATION`
  *(Both may consume shared observation extraction libraries, but neither engine depends on the other).*

---

## 3. Engine Return Contract & Data Types

FASHIONISTA requires no knowledge of the application's underlying data layer. It receives a pure observation and returns an immutable snapshot with strict versioning.

```kotlin
/**
 * Pure observation layer. FASHIONISTA does not care where these came from 
 * (Camera, Wardrobe, Pinterest, or Flat-lay).
 */
data class FashionistaObservation(
    val garments: List<GarmentObservation>,
    val cosmetics: List<CosmeticObservation>,
    val silhouetteMask: SilhouetteObservation?,
    val textureObservations: List<TextureObservation>,
    val wearer: WearerObservation?
)

/**
 * Immutable snapshot emitted by the offline FASHIONISTA Engine.
 */
data class FashionistaResult(
    val aestheticScore: Float,       // 0.0 to 100.0 (Reference-calibrated aesthetic score)
    val coverage: Float,             // 0.0 to 1.0 (Completeness of the available evidence)
    val radarBreakdown: RadarMetrics,
    val calibrationVersion: String   // e.g., "FASHIONISTA Standard v1.1"
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

## 4. Score vs. Coverage Semantics

Missing physical data does not artificially degrade an outfit's beauty. The metrics represent two entirely distinct concepts:

* **Score:** The aesthetic quality of the available evidence.
* **Coverage:** The completeness of the available evidence.

*Interpretations:*

* **High Score + Low Coverage:** "Looks excellent based on limited evidence." (e.g., A well-composed flat-lay photograph lacking wearer integration).
* **High Score + High Coverage:** "Looks excellent based on comprehensive evidence." (e.g., A full-body photograph with accurate skin, hair, and eye telemetry).

---

## 5. Step-by-Step Implementation Guide to Build the Separation

### Step 1: Decouple Personal Color from FASHIONISTA

When the user manually edits telemetry (Skin/Eye/Hair), the UI should route this state to the respective systems independently.

```kotlin
private fun updateManualTelemetry(newTelemetry: FaceTelemetryData) {
    _faceTelemetry.value = newTelemetry

    // 1. Personal Color Engine (Strictly handles identity & persistence)
    viewModelScope.launch {
        val season = personalColorEngine.classify(newTelemetry)
        val profile = ColorProfile(season, newTelemetry)
        fashionRepository.saveProfile(profile)
    }

    // 2. FASHIONISTA Engine (Strictly evaluates the current visual state)
    val currentObservation = FashionistaObservation(
        garments = _currentGarmentObservations.value,
        cosmetics = _currentCosmeticObservations.value,
        silhouetteMask = null, // Or pulled from vision ML
        textureObservations = emptyList(),
        wearer = WearerObservation(newTelemetry) // Wearer updated here
    )
    
    val result = fashionistaEngine.evaluate(currentObservation)
    _fashionistaMetrics.value = result 
}

```

---

## 6. Mathematical Evaluation & Determinism

FASHIONISTA utilizes **100% Deterministic Mathematical Evaluation**.
Color analysis uses CIELAB / L*C*h° and perceptual color-difference mathematics where applicable. This runs alongside discrete algorithms for composition, visual mass, silhouette proportions, and spatial texture matrices (GLCM/Gabor).

Once an identical `FashionistaObservation` is supplied, FASHIONISTA produces the exact same deterministic result independent of network availability, user context, wardrobe state, or provenance.