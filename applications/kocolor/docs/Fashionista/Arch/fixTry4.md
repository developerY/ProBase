# FASHIONISTA Engine vs. KoColor Recommendation Pipeline Architecture

This document defines the strict architectural boundary between **FASHIONISTA** (the standalone, 100% offline, zero-network deterministic aesthetic evaluation engine) and the **KoColor Recommendation Engine** (the AI-assisted style architect for daily outfit synthesis).

---

## 1. Architectural Separation

| Dimension | KoColor Recommendation Engine | FASHIONISTA Evaluation Engine |
| --- | --- | --- |
| **Primary Question** | *"What should I wear?"* | *"How good is this?"* |
| **Input** | Context Stream (Weather, Occasion, Wardrobe, User Intent) | `FashionistaObservation` + `FashionistaCalibration` |
| **Network & AI** | Optional LLM (Firebase Vertex AI / Gemini / Local AI) | **ZERO Network, ZERO LLM, 100% Offline Deterministic Computation** |
| **Candidate Retrieval** | Candidate Search, Filtering & Ranking | **NO Retrieval, NO Wardrobe Search** |
| **Output** | Selected Outfit + Editorial AI Rationale | `FashionistaResult` (`aestheticScore`, `coverage`, 6-Axis Radar) |
| **Provenance** | Internal Wardrobe Inventory Only | **Provenance Agnostic** (Accepts arbitrary observed ensembles) |

---

## 2. Hard Boundary Flow

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
     Occasion                              +
     User Intent                    FashionistaCalibration
     Wardrobe                              │
     Retrieval                             ▼
     Ranking                     Deterministic Computation
     Gemini / LLM                          │
             │                             ▼
             ▼                      FashionistaResult
       Selected Outfit               ├─ Score 0–100
       + Rationale                   ├─ Coverage 0–1
                                     ├─ 6-Axis Radar
                                     └─ Calibration Version

```

**Critical Dependency Rule:**

* `RECOMMENDATION` ───────X──────► `FASHIONISTA`
* `FASHIONISTA` ──────────X──────► `RECOMMENDATION`

*(Both engines may consume shared deterministic extraction libraries, but neither engine depends on the other).*

---

## 3. Dependency Firewall

To ensure total isolation and ease of code review, the boundaries of the FASHIONISTA module are strictly defined:

**FASHIONISTA MUST NOT depend on:**

* Repository or Database layers
* `WardrobeItem` or application-specific product entities
* User profiles or seasonal identity state
* Weather or atmospheric conditions
* Occasion or calendar context
* User intent or preference history
* Recommendation pipeline outputs
* Network transports or remote APIs
* LLM / Generative AI SDKs
* Application UI state

**FASHIONISTA MAY depend on:**

* `FashionistaObservation`
* `FashionistaCalibration`
* Deterministic mathematical, color-space, and computer vision primitives

---

## 4. Engine Return Contract & Data Types

FASHIONISTA receives an immutable observation containing purely visual properties and a frozen calibration standard, returning an immutable evaluation snapshot:

```kotlin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Pure observation layer containing strictly visual properties.
 * Free of application database IDs and entity references.
 */
data class FashionistaObservation(
    val garments: ImmutableList<GarmentObservation>,
    val cosmetics: ImmutableList<CosmeticObservation>,
    val silhouetteMask: SilhouetteObservation? = null,
    val textureObservations: ImmutableList<TextureObservation> = persistentListOf(),
    val wearer: WearerObservation? = null
)

/**
 * Immutable evaluation snapshot emitted by the offline engine.
 * 
 * Invariants:
 * - aestheticScore ∈ [0.0, 100.0]
 * - coverage ∈ [0.0, 1.0]
 * - all radar metrics ∈ [0.0, 1.0]
 * - calibrationVersion identifies the active scoring standard
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

---

## 5. The Determinism Contract

FASHIONISTA operates under strict mathematical predictability:

$$\text{FashionistaObservation} + \text{FashionistaCalibration} = \text{FashionistaResult}$$

Given an identical `FashionistaObservation` and identical `FashionistaCalibration`, FASHIONISTA produces the exact same deterministic result across all platforms. The engine enforces:

* Fixed iteration order for all collection processing.
* No unordered parallel reductions.
* Deterministic floating-point operations with explicitly defined numerical behavior.
* Deterministic image preprocessing and color conversions (CIELAB / $L^*C^*h^\circ$).
* Deterministic tie handling.
* Zero random seeds, random sampling, timestamps, or device-specific environmental inputs.

---

## 6. Score vs. Coverage Semantics

Missing physical data does not artificially degrade an outfit's aesthetic score:

* **Score:** The aesthetic quality of the available evidence.
* **Coverage:** The completeness of the available evidence.

If a flat-lay photograph is evaluated (no face detected), the wearer evidence is unavailable. Because the wearer contribution is excluded, the **Coverage** decreases, but the **Score** is not artificially penalized:

* **High Score + Low Coverage:** *"Looks excellent based on limited evidence."*
* **High Score + High Coverage:** *"Looks excellent based on comprehensive evidence."*

---

## 7. UI & State Flow Decoupling

When the user manually edits telemetry (Skin/Eye/Hair) in the UI, the ViewModel routes state to the respective systems independently:

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

    // 2. FASHIONISTA Engine (Evaluates the current observed ensemble)
    val currentObservation = FashionistaObservation(
        garments = _currentGarmentObservations.value.toImmutableList(),
        cosmetics = _currentCosmeticObservations.value.toImmutableList(),
        silhouetteMask = currentMask,
        textureObservations = currentTextures.toImmutableList(),
        wearer = WearerObservation(newTelemetry) 
    )
    
    _fashionistaMetrics.value = fashionistaEngine.evaluate(
        observation = currentObservation,
        calibration = activeCalibrationStandard
    )
}

```

---

## 8. Verification Checklist

* [x] **Dependency Firewall Enforced**: FASHIONISTA has zero dependencies on `fashionRepository`, `WardrobeItem`, UI state, or database models.
* [x] **Zero Network Requests on Telemetry Edits**: Tapping skin/eye/hair swatches does not trigger Firebase/Gemini API calls.
* [x] **Immutable Observation Contracts**: `FashionistaObservation` uses `ImmutableList` with zero entity/database ID dependencies.
* [x] **Strict Determinism Invariant**: $\text{Observation} + \text{Calibration} \rightarrow \text{Result}$ with zero random seeds, timestamps, or unordered reductions.
* [x] **Score vs. Coverage Disambiguation**: Missing wearer telemetry reduces `coverage`, not the intrinsic `aestheticScore`.
* [x] **Arbitrary Ensemble Support**: FASHIONISTA evaluates any observed ensemble (flat-lay, 2-piece, 3-piece, or full-body worn photo) regardless of provenance.