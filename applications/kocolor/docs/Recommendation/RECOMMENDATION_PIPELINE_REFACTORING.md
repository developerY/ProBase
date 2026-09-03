# KoColor Recommendation Pipeline Refactoring & Validation Specification

This document details the refactoring of the **KoColor Recommendation Pipeline** (`StyleSimulatorEngine`, `PromptAssembler`, `RecommendationValidator`, `WardrobeCandidateFilter`) to eliminate LLM hallucinations, enforce dynamic prompt construction, implement post-LLM validation, and apply relational cosmetic scoring.

---

## 1. Executive Summary

While the **FASHIONISTA Evaluation Engine** handles pure offline aesthetic scoring, the **KoColor Recommendation Pipeline** is responsible for generating personalized daily style recommendations ("What should I wear?").

### Key Objectives Achieved:
1. **Dynamic Pre-Flight Prompt Construction**: Automatically adapts prompt requests (`Select BEST 3 clothing items`, `Select BEST 2 clothing items`) based strictly on available categories in the candidate manifest.
2. **Strict Grounding Rule**: Enforces an explicit system instruction preventing LLMs from inventing ungrounded stylistic adjectives (e.g. calling seamless nylon "structural").
3. **Deterministic Post-LLM Validator (`RecommendationValidator.kt`)**: Intercepts, validates, and sanitizes raw JSON from Gemini before it reaches UI state flows.
4. **Relational Cosmetic Scoring**: Replaces flat `3.10` scores with a dynamic mathematical temperature delta (`3.85`, `3.60`, `3.10`) matching candidate cosmetics against locked outfit anchors.
5. **Missing Category & Slot Fallback**: Dynamically adapts prompt constraints and post-validation assertions when wardrobe categories (e.g., shoes) are sparse.

---

## 2. Refactoring Tasks & Code Changes

### Task 1: Typed Dynamic Pre-Flight Prompt Construction & Grounding
**File**: [`PromptAssembler.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt)

* **Typed Candidate Evaluation**: Inspects `clothingCandidates` and `cosmeticCandidates` domain objects (avoiding string parsing on serialized manifests) to construct exact category goals:
  ```kotlin
  val availableClothingCategories = clothingCandidates.mapNotNull { it.clothingItem?.category }.toSet()
  val hasShoes = clothingCandidates.isEmpty() || availableClothingCategories.contains(ClothingCategory.SHOES)

  val clothingGoal = if (hasShoes) {
      "1. Select BEST 3 clothing items (1 Top, 1 Bottom, 1 Shoes) from the WARDROBE section."
  } else {
      "1. Select BEST 2 clothing items (1 Top, 1 Bottom) from the WARDROBE section."
  }
  ```
* **Strict Grounding Instruction Added**:
  ```text
  STRICT GROUNDING RULE:
  Do not invent stylistic adjectives (e.g., do not call nylon 'structural'). Describe items strictly using the physical materials and attributes listed in the manifest.
  ```

---

### Task 2: Deterministic Post-LLM Validator
**File**: [`RecommendationValidator.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RecommendationValidator.kt)

Interceptors raw Gemini JSON and performs three verification passes:

1. **ID Existence Check**: Asserts every ID in `selectedClothingIds` and `selectedCosmeticIds` actually exists in the candidate manifest. Removes invalid or hallucinated IDs.
2. **Rationale Cross-Check & Sanitization**: Scans `rationale` for product names. If a product name is referenced in `rationale` but its ID is missing from `selectedClothingIds` or `selectedCosmeticIds`, strips that sentence from the rationale:
   ```kotlin
   clothingMap.forEach { (name, item) ->
       val id = "w_${item.internalId}"
       if (id !in filteredClothingIds && sanitizedRationale.contains(name, ignoreCase = true)) {
           Log.w("RecommendationValidator", "Sanitizing rationale: stripping reference to unselected clothing '$name'")
           sanitizedRationale = sanitizedRationale.replace(Regex("(?i)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\."), "")
       }
   }
   ```
3. **Integration**: Integrated into [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt#L150) prior to caching or UI emission.

---

### Task 3: Relational Cosmetic Scoring
**File**: [`WardrobeCandidateFilter.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt#L145)

Replaced flat `3.10` cosmetic scores with a relational temperature delta matching candidate cosmetics against the locked outfit anchor and user appearance profile:

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

* **Result**: Produces a mathematical ranking delta (`3.85`, `3.60`, `3.10`) that orders cosmetics by temperature harmony.

---

### Task 4: Slot Deduplication & Missing Category Fallback
**File**: [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt#L250)

* **Pre-Flight Category Diversity Check**: Audits `topWardrobeProv` to verify `TOPS`/`DRESSES`, `BOTTOMS`, and `SHOES` are present in candidates.
* If a category is missing (e.g. 0 shoes in top $K$), automatically injects top supplementary candidates for that missing category before constructing the prompt.
* If 0 shoes exist in the user's entire closet, prompt adapts dynamically to request 2 items (`Top, Bottom`), and `RecommendationValidator` adjusts assertions accordingly.

---

## 3. Verification & Test Results

* **Unit Tests**: Executed `:applications:kocolor:data:testDebugUnitTest` and `:applications:kocolor:features:analyzer:testDebugUnitTest`. **35 out of 35 unit tests passed 100% green**.
* **Debug Build**: `:applications:kocolor:apps:mobile:assembleDebug` compiled and assembled successfully with 0 errors.
