# KoColor Recommendation Pipeline Refactoring & Validation Specification

This document details the refactoring of the **KoColor Recommendation Pipeline** (`StyleSimulatorEngine`, `PromptAssembler`, `RecommendationValidator`, `WardrobeCandidateFilter`, `GreedyRehydrator`). It establishes a deterministic-first recommendation architecture where Gemini acts purely as a synthesis layer, while strictly typed deterministic code controls evidence, constraints, validation, and persistence.

---

## 1. System Intent & Architecture

While **FASHIONISTA** operates as an independent, offline measurement tool ("How good is this outfit?"), the **KoColor Recommendation Engine** serves as the AI style architect ("What should I wear today?").

This refactoring hardens the recommendation engine against LLM hallucinations, schema violations, and ungrounded claims by enforcing typed domain boundaries before and after the generative step.

---

## 2. Task 1: Typed Dynamic Prompt Construction & Grounding

### File Modified:

* `PromptAssembler.kt`

### Implementation:

Prompt instructions are now built deterministically from typed candidate structures, strictly avoiding fragile `String.contains()` checks on serialized manifests.

```kotlin
// 1. Typed evaluation of clothing candidate availability
val availableClothingCategories = clothingCandidates.map { it.category }.toSet()
val hasShoes = "SHOES" in availableClothingCategories

val clothingGoal = if (hasShoes) {
    "1. Select BEST 3 clothing items (1 Top, 1 Bottom, 1 Shoes) from the WARDROBE section."
} else {
    "1. Select BEST 2 clothing items (1 Top, 1 Bottom) from the WARDROBE section."
}

// 2. Explicit domain mapping for cosmetics
enum class CosmeticRole { EYE, CHEEK, LIP, NAIL }

val availableCosmeticRoles = cosmeticCandidates.mapNotNull { 
    when (it.category.uppercase()) {
        "EYES" -> CosmeticRole.EYE
        "DIMENSION", "CHEEK" -> CosmeticRole.CHEEK
        "LIPS" -> CosmeticRole.LIP
        "NAILS" -> CosmeticRole.NAIL
        else -> null
    }
}.toSet()

val cosmeticGoal = if (availableCosmeticRoles.isNotEmpty()) {
    val rolesText = availableCosmeticRoles.joinToString(", ") { it.name }
    "2. Select 1 item for each available cosmetic role ($rolesText) from the COSMETICS section."
} else {
    "2. Select available cosmetic items from the COSMETICS section."
}

```

### Strict Grounding Rule:

```text
STRICT GROUNDING RULE:
Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest.

```

---

## 3. Task 2: Deterministic Post-LLM Validator

### Files Created/Modified:

* `RecommendationValidator.kt`

### Implementation:

The validator now enforces full compositional invariants rather than merely checking for ID existence.

1. **The Validation Pipeline**:
* **ID Exists**: Asserts every parsed ID matches the candidate manifest.
* **No Duplicate IDs**: Rejects sets containing repeated items.
* **Valid Category**: Maps IDs back to domain entities.
* **Required Role Coverage**: Asserts the presence of `TOP` and `BOTTOM` (and `SHOES` if requested).
* **Cardinality**: Asserts exactly 3 (or 2) clothing items and the exact number of requested cosmetics.
* **Accept / Repair / Reject**: Throws an internal retry exception if compositional invariants fail.


2. **Rationale Handling**:
   While a fallback regex sanitizes rogue references (`(?i)[^.]*\b${Regex.escape(name)}\b[^.]*\.`), the primary architectural goal is to rely entirely on the structured LLM ID arrays and generate final human-readable product names/material claims locally.

---

## 4. Task 3: Relational Cosmetic Temperature Scoring

### File Modified:

* `WardrobeCandidateFilter.kt`

### Implementation:

Cosmetics are ranked dynamically by color temperature compatibility against the locked outfit anchor.

> **Important Documentation Note:** The constants used here (`1.85`, `1.25`, `0.60`) are **deterministic recommendation heuristics** designed strictly to force ranking deltas prior to LLM synthesis. They are *not* FASHIONISTA aesthetic calibration parameters.

```kotlin
private fun calculateCosmeticScore(item: CosmeticItem, context: StyleRequestContext): Double {
    var score = 1.0 
    val appearance = context.appearanceProfile
    val telemetry = context.appearanceTelemetry

    val isWarmContext = telemetry.undertoneScore > 0.02f ||
            appearance.undertone.contains("Warm", ignoreCase = true)
    val isCoolContext = telemetry.undertoneScore < -0.02f ||
            appearance.undertone.contains("Cool", ignoreCase = true)

    val cosmeticTemp = item.temperature.name.uppercase()
    when {
        isWarmContext && (cosmeticTemp.contains("WARM") || cosmeticTemp.contains("GOLDEN")) -> score += 1.85
        isCoolContext && (cosmeticTemp.contains("COOL") || cosmeticTemp.contains("ROSY")) -> score += 1.85
        cosmeticTemp.contains("NEUTRAL") -> score += 1.25
        else -> score += 0.60
    }
    return score
}

```

---

## 5. Task 4: Ghost Anchor Resolution & Explicit Slot Deduplication

### File Modified:

* `GreedyRehydrator.kt`

### Implementation:

1. **Ghost Anchors**: `selectionState.activeAnchors` are prepended into `topWardrobeProv`, guaranteeing locked anchors are serialized into the manifest.
2. **Explicit Slot Policy**: Deduplication is enforced via a strict domain invariant rather than an accidental consequence of category strings.

```kotlin
enum class OutfitSlot {
    TOP,
    BOTTOM,
    SHOES,
    OUTERWEAR
}

// Maps candidate categories to strict OutfitSlots and deduplicates by slot
// ensuring safe expansion if ACCESSORY or BAG categories are added later.

```

---

## 6. Task 5: FASHIONISTA Score Persistence

### Files Modified:

* `FashionModels.kt`

### Implementation:

The dangerous silent default of `88` is removed. The persisted score is strictly defined as the offline FASHIONISTA calculation, explicitly preserving its calibration provenance.

```text
Recommendation Engine
        │ (generates outfit)
        ▼
Selected Ensemble
        │ (observed / represented as FashionistaObservation)
        ▼
FASHIONISTA Evaluation Engine
        │
        ▼
aestheticScore + coverage + radar
        │
        ▼
History Persistence

```

```kotlin
@Serializable
data class FashionAdvice(
    val id: String,
    // ...
    val fashionistaScore: Int? = null,
    val fashionistaCalibrationVersion: String? = null
)

```

---

## 7. Build & Verification Results

* **Unit Tests**: Executed `:applications:kocolor:data:testDebugUnitTest` and `:applications:kocolor:features:analyzer:testDebugUnitTest`. **35 out of 35 unit tests passed 100% green**.
* **Debug Build**: `:applications:kocolor:apps:mobile:assembleDebug` assembled successfully with 0 errors.
* **Release Build**: `:applications:kocolor:apps:mobile:assembleRelease` assembled with R8 log stripping enabled and 0 errors.