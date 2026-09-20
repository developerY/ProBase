# Technical Walkthrough: KoColor Recommendation Pipeline Refactoring & Validation

This document provides a comprehensive technical walkthrough of the refactoring implemented in the **KoColor Recommendation Engine** (`StyleSimulatorEngine`, `PromptAssembler`, `RecommendationValidator`, `WardrobeCandidateFilter`, `GreedyRehydrator`).

---

## 1. System Intent & Architecture

While **FASHIONISTA** operates as an independent, offline $L^*C^*h^\circ$ mathematical calculator ("How good is this outfit?"), the **KoColor Recommendation Engine** serves as the AI style architect ("What should I wear today?").

This refactoring hardens the recommendation engine against LLM hallucinations, schema violations, and ungrounded stylistic claims by boxing Gemini in with dynamic pre-flight prompt construction and deterministic post-flight output validation.

---

## 2. Task 1: Dynamic Pre-Flight Prompt Construction & Grounding

### File Modified:
* [`PromptAssembler.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt)

### Implementation:
Instead of hardcoding `"Select BEST 3 clothing items (Top, Bottom, Shoes)"` and `"Select 4 cosmetic items (1 Eye, 1 Cheek, 1 Lip, 1 Nail)"`, `PromptAssembler` inspects the candidate manifest to dynamically construct instructions based strictly on available categories:

```kotlin
val clothingGoal = if (compactManifest.contains("SHOES", ignoreCase = true)) {
    "1. Select BEST 3 clothing items (1 Top, 1 Bottom, 1 Shoes) from the WARDROBE section."
} else {
    "1. Select BEST 2 clothing items (1 Top, 1 Bottom) from the WARDROBE section."
}

val cosmeticCategories = mutableListOf<String>()
if (compactManifest.contains("EYES", ignoreCase = true)) cosmeticCategories.add("Eye")
if (compactManifest.contains("DIMENSION", ignoreCase = true) || compactManifest.contains("CHEEK", ignoreCase = true)) cosmeticCategories.add("Cheek")
if (compactManifest.contains("LIPS", ignoreCase = true)) cosmeticCategories.add("Lip")
if (compactManifest.contains("NAILS", ignoreCase = true)) cosmeticCategories.add("Nail")

val cosmeticGoal = if (cosmeticCategories.isNotEmpty()) {
    "2. Select 1 item from each available cosmetic category (${cosmeticCategories.joinToString(", ")}) from the COSMETICS section."
} else {
    "2. Select available cosmetic items from the COSMETICS section."
}
```

### Strict Grounding Rule:
Added an explicit system instruction preventing the model from hallucinating ungrounded stylistic adjectives:
```text
STRICT GROUNDING RULE:
Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest.
```

---

## 3. Task 2: Deterministic Post-LLM Validator

### Files Created/Modified:
* [`RecommendationValidator.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RecommendationValidator.kt)
* [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)

### Implementation:
`RecommendationValidator` intercepts the raw JSON string from Gemini before it reaches UI state flows and performs three verification passes:

1. **ID Existence Verification**: Asserts that every ID in `selectedClothingIds` and `selectedCosmeticIds` exists in the `AVAILABLE CANDIDATES` list. Removes hallucinated IDs.
2. **Rationale Sanitization (Regex Boundary Pass)**: Scans `rationale` for product names. If a product is mentioned in `rationale` but its ID is missing from selected arrays, safely strips that sentence from the rationale:
   ```kotlin
   clothingMap.forEach { (name, item) ->
       val id = "w_${item.internalId}"
       if (id !in filteredClothingIds && sanitizedRationale.contains(name, ignoreCase = true)) {
           Log.w("RecommendationValidator", "Sanitizing rationale: stripping reference to unselected clothing '$name'")
           sanitizedRationale = sanitizedRationale.replace(Regex("(?i)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\."), "")
       }
   }
   ```
3. **Execution Integration**:
   ```kotlin
   val rawBlueprint = decodeBlueprint(rawResult)
   val validation = validator.validateAndSanitize(
       rawBlueprint = rawBlueprint,
       clothingCandidates = fitResult.clothingCandidates,
       cosmeticCandidates = fitResult.cosmeticCandidates
   )
   val blueprint = validation.sanitizedBlueprint
   ```

---

## 4. Task 3: Relational Cosmetic Temperature Scoring

### File Modified:
* [`WardrobeCandidateFilter.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt)

### Implementation:
Replaced flat `3.10` cosmetic scores with relational color temperature scoring evaluating both raw float thresholds (`appearanceTelemetry.undertoneScore > 0.02f`) and classified descriptors (`appearanceProfile.undertone.contains("Warm")`):

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

## 5. Task 4: Ghost Anchor Resolution & Category Diversity Guarantee

### Files Modified:
* [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
* [`GreedyRehydrator.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/GreedyRehydrator.kt)

### Implementation:
1. **Ghost Anchor Resolution**: Converts `selectionState.activeAnchors` into `CandidateProvenance` and explicitly prepends them into `topWardrobeProv`, guaranteeing locked anchors are serialized into the candidate manifest.
2. **Pre-Flight Category Diversity Check**: Audits candidate items to ensure `TOPS`/`DRESSES`, `BOTTOMS`, and `SHOES` are all represented in the manifest before invoking Gemini.
3. **Slot Deduplication Invariant**: Enforces `distinctBy { it.category }` in `GreedyRehydrator`, constraining outfits to **at most 1 TOP**, **1 BOTTOM**, **1 SHOES**, and **1 OUTERWEAR**.

---

## 6. Task 5: History Score Persistence

### Files Modified:
* [`FashionModels.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/core/model/src/main/java/com/zoewave/probase/core/model/ritual/FashionModels.kt)
* [`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
* [`VisualBlueprintModels.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/graphics/VisualBlueprintModels.kt)

### Implementation:
1. Added `@Serializable val fashionistaScore: Int = 88` to `FashionAdvice`.
2. Updated `saveSelectionToColorTab()` in `StyleSimulatorViewModel.kt` to pass the exact calculated `koColorScore` (e.g., `93`) when persisting suggestions to database history.
3. Updated `FashionAdvice.toVisualBlueprintData()` to map `koColorScore = fashionistaScore`, resolving the bug where historical items displayed a default `88`.

---

## 7. Build & Verification Results

* **Unit Tests**: Executed `:applications:kocolor:data:testDebugUnitTest` and `:applications:kocolor:features:analyzer:testDebugUnitTest`. **35 out of 35 unit tests passed 100% green**.
* **Debug Build**: `:applications:kocolor:apps:mobile:assembleDebug` assembled successfully with 0 errors.
* **Release Build**: `:applications:kocolor:apps:mobile:assembleRelease` assembled with R8 log stripping enabled and 0 errors.
