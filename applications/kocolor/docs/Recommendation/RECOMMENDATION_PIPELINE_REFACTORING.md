# KoColor Recommendation Pipeline Refactoring & Validation Specification

This document details the refactoring of the **KoColor Recommendation Pipeline** (`StyleSimulatorEngine`, `PromptAssembler`, `RecommendationValidator`, `WardrobeCandidateFilter`, `GreedyRehydrator`). It establishes a deterministic-first recommendation architecture where Gemini acts as a constrained synthesis and selection layer, while strictly typed deterministic code controls evidence, constraints, validation, and persistence.

---

## 1. System Intent & Architecture

While **FASHIONISTA** operates as an independent, offline measurement tool ("How good is this outfit?"), the **KoColor Recommendation Engine** serves as the AI style architect ("What should I wear today?").

This refactoring hardens the recommendation engine to contain LLM hallucinations and prevent ungrounded output from entering trusted application state by enforcing typed domain boundaries before and after the generative step.

---

## 2. Task 1: Typed Dynamic Prompt Construction & Grounding

### File Modified:
* [`PromptAssembler.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt)

### Implementation:
Prompt instructions are built deterministically from typed candidate structures, ensuring prompt requirements and validator assertions derive from a common source of truth:

```kotlin
// 1. Typed evaluation of clothing candidate availability
val availableClothingCategories = clothingCandidates.mapNotNull { it.clothingItem?.category }.toSet()
val hasShoes = availableClothingCategories.contains(ClothingCategory.SHOES) ||
        (clothingCandidates.isEmpty() && compactManifest.contains("SHOES", ignoreCase = true))

val clothingGoal = if (hasShoes) {
    "1. Select BEST 3 clothing items (1 Top, 1 Bottom, 1 Shoes) from the WARDROBE section."
} else {
    "1. Select BEST 2 clothing items (1 Top, 1 Bottom) from the WARDROBE section."
}

// 2. Typed evaluation of cosmetic candidate availability
val availableCosmeticCategories = cosmeticCandidates.mapNotNull { it.cosmeticItem?.macroCategory }.toSet()
val cosmeticCategories = mutableListOf<String>()
if (availableCosmeticCategories.contains(MacroCategory.EYES)) cosmeticCategories.add("Eye")
if (availableCosmeticCategories.contains(MacroCategory.DIMENSION)) cosmeticCategories.add("Cheek")
if (availableCosmeticCategories.contains(MacroCategory.LIPS)) cosmeticCategories.add("Lip")
if (availableCosmeticCategories.contains(MacroCategory.NAILS)) cosmeticCategories.add("Nail")

val cosmeticGoal = if (cosmeticCategories.isNotEmpty()) {
    "2. Select 1 item from each available cosmetic role (${cosmeticCategories.joinToString(", ")}) from the COSMETICS section."
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

### File Created:
* [`RecommendationValidator.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RecommendationValidator.kt)

### Implementation:
The validator enforces deterministic compositional invariants:

1. **Grounded ID Check**: Rejects or removes IDs that are not present in the grounded candidate manifest.
2. **Duplicate-ID Rejection**: Rejects or removes duplicate ID references.
3. **Category & Role Validity**: Maps IDs back to domain entities to verify category classification.
4. **Required Role Coverage**: Verifies selected items satisfy requested TOP/BOTTOM/SHOES composition goals.
5. **Cardinality Constraints**: Asserts exactly 3 (or 2) clothing items and requested cosmetic roles.
6. **Rationale Sentence Sanitization**: Scans `rationale` for product names. If a product is mentioned in `rationale` but its ID is unselected, strips that sentence from `rationale`:
   ```kotlin
   clothingMap.forEach { (name, item) ->
       val id = "w_${item.internalId}"
       if (id !in filteredClothingIds && sanitizedRationale.contains(name, ignoreCase = true)) {
           Log.w("RecommendationValidator", "Sanitizing rationale: stripping reference to unselected clothing '$name'")
           sanitizedRationale = sanitizedRationale.replace(Regex("(?i)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\."), "")
       }
   }
   ```
7. **Pipeline Integration**: Integrated directly into [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt#L150) prior to caching or UI emission.

---

## 4. Task 3: Relational Cosmetic Temperature Scoring

### File Modified:
* [`WardrobeCandidateFilter.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt#L145)

### Implementation:
Cosmetics are ranked dynamically by color temperature compatibility against the user's appearance profile and telemetry.

> **Note**: The constants (`1.85`, `1.25`, `0.60`) are deterministic recommendation heuristics designed to create ranking deltas prior to LLM synthesis. They are separate from FASHIONISTA calibration parameters.

```kotlin
private fun calculateCosmeticScore(item: CosmeticItem, context: StyleRequestContext): Double {
    var score = 1.0 // Base score
    val appearance = context.appearanceProfile
    val telemetry = context.appearanceTelemetry

    // Dual evaluation: raw float threshold + string descriptor
    val isWarmContext = telemetry.undertoneScore > 0.02f ||
            appearance.undertone.contains("Warm", ignoreCase = true) ||
            appearance.undertone.contains("Golden", ignoreCase = true) ||
            appearance.undertone.contains("Peach", ignoreCase = true)

    val isCoolContext = telemetry.undertoneScore < -0.02f ||
            appearance.undertone.contains("Cool", ignoreCase = true) ||
            appearance.undertone.contains("Pink", ignoreCase = true) ||
            appearance.undertone.contains("Blue", ignoreCase = true)

    val cosmeticTemp = item.temperature.name.uppercase()
    when {
        isWarmContext && (cosmeticTemp.contains("WARM") || cosmeticTemp.contains("GOLDEN")) -> score += 1.85
        isCoolContext && (cosmeticTemp.contains("COOL") || cosmeticTemp.contains("ROSY")) -> score += 1.85
        cosmeticTemp.contains("NEUTRAL") -> score += 1.25
        else -> score += 0.60
    }

    val keywords = context.intent.lowercase().split(" ", ",", ".")
    if (keywords.any { item.name.contains(it, ignoreCase = true) || (item.notes?.contains(it, ignoreCase = true) ?: false) }) {
        score += 0.75
    }

    if (item.isSignature) score += 0.25

    return score
}
```

---

## 5. Task 4: Ghost Anchor Resolution & Slot Policy Enforcement

### File Modified:
* [`GreedyRehydrator.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/GreedyRehydrator.kt)
* [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt#L250)

### Implementation:
1. **Ghost Anchors**: `selectionState.activeAnchors` are prepended into `topWardrobeProv`, guaranteeing locked anchors are serialized into the manifest.
2. **Candidate Diversity**: Pre-audits candidates to ensure `TOPS`/`DRESSES`, `BOTTOMS`, and `SHOES` are represented before constructing the prompt.
3. **If No `SHOES` Candidate Exists in Wardrobe Inventory**: The prompt adapts dynamically to request 2 items (`Top, Bottom`), and `RecommendationValidator` adjusts assertions accordingly.

---

## 6. Task 5: FASHIONISTA Score Persistence

### Files Modified:
* [`FashionModels.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/core/model/src/main/java/com/zoewave/probase/core/model/ritual/FashionModels.kt)
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`VisualBlueprintModels.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/simulator/ui/components/graphics/VisualBlueprintModels.kt)

### Implementation:
The persisted score in history is strictly defined as the calculated FASHIONISTA score (`fashionistaScore`), resolving the issue where historical items displayed a default `88`.

```text
Recommendation Engine
        │ (generates outfit)
        ▼
Selected Ensemble
        │ (observed as FashionistaObservation)
        ▼
FASHIONISTA Evaluation Engine
        │
        ▼
aestheticScore + coverage + radar
        │
        ▼
History Persistence
```

---

## 7. Build & Verification Results

* **Unit Tests**: Executed `:applications:kocolor:data:testDebugUnitTest` and `:applications:kocolor:features:analyzer:testDebugUnitTest`. **35 out of 35 unit tests passed 100% green**.
* **Debug Build**: `:applications:kocolor:apps:mobile:assembleDebug` assembled successfully with 0 errors.
* **Release Build**: `:applications:kocolor:apps:mobile:assembleRelease` assembled with R8 log stripping enabled and 0 errors.
