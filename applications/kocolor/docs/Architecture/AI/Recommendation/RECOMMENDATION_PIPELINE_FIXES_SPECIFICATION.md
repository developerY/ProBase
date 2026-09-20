# KoColor Recommendation Pipeline: Implementation Specification for Remaining Fixes

This document details the step-by-step technical implementation for the final 3 recommendation pipeline refinements identified in `ArchReview31.md` and `Fix31.md`.

---

## 1. Issue & Resolution Summary

| Issue | Root Cause | Implementation Fix |
| :--- | :--- | :--- |
| **1. Cosmetic Score 3.10 Flatline** | `StyleSimulatorEngine.kt` was mapping `cCandidatesProv` with hardcoded scores (`0.5 + 0.8 + 0.8 + 1.0 = 3.10`) instead of using calculated relational scores. | Updated `WardrobeCandidateFilter.getCosmeticCandidateProvenance()` to compute and return full `CandidateProvenance` objects with temperature-relational scores (`3.85`, `3.60`, `3.10`). |
| **2. Weather Telemetry Collision** | `PromptAssembler.kt` concatenated `context.weather` (which already had fallback text) with `Temp: 22°C`, producing `UV: Unknown, Temp: UnknownC (Temp: 22.0°C, UV: 3.0)`. | Enforced single source-of-truth weather string formatting in `PromptAssembler.kt`. |
| **3. Domain Role Alignment** | `DIMENSION` was converted to `Cheek` ad-hoc in prompt string interpolation without explicit domain mapping. | Created `CosmeticRole` enum (`EYE`, `CHEEK`, `LIP`, `NAIL`, `PREP`) to centralize domain role mapping across candidate filter, prompt assembler, and validator. |

---

## 2. Code Implementation Details

### 1. Fixing the 3.10 Cosmetic Flatline
**Files**: [`WardrobeCandidateFilter.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt) & [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)

In `WardrobeCandidateFilter.kt`:
```kotlin
suspend fun getCosmeticCandidateProvenance(
    inventory: List<CosmeticItem>,
    context: StyleRequestContext,
    limit: Int
): List<CandidateProvenance> {
    val noiseCategories = setOf("oral", "tools", "fragrance", "grooming", "organizers")
    
    val eligibleItems = inventory.filter { item ->
        !item.isHidden && 
        !noiseCategories.contains(item.macroCategory.name.lowercase()) &&
        !isCosmeticRotationViolated(item)
    }

    val anchoredItems = eligibleItems.filter { "c_${it.internalId}" in context.anchoredCosmeticIds }
    val remainingItems = eligibleItems.filter { "c_${it.internalId}" !in context.anchoredCosmeticIds }

    // Calculate relational scores (3.85, 3.60, 3.10)
    val rankedRemaining = remainingItems.map { item ->
        val score = calculateCosmeticScore(item, context)
        CandidateProvenance(
            cosmeticItem = item,
            contextScore = score.toFloat(),
            colorScore = 0.8f,
            appearanceScore = 0.8f,
            freshnessScore = 1.0f,
            retrievalReason = "Relational temperature match (${item.temperature.name})"
        )
    }.sortedByDescending { it.totalScore }

    val anchoredProv = anchoredItems.map { item ->
        CandidateProvenance(
            cosmeticItem = item,
            contextScore = 2.0f,
            colorScore = 1.0f,
            appearanceScore = 1.0f,
            freshnessScore = 1.0f,
            retrievalReason = if (item.isSignature) "[Signature Item] Rotation bypassed." else "[LOCKED ANCHOR] Required cosmetic anchor"
        )
    }

    val targetCategories = setOf(MacroCategory.EYES, MacroCategory.DIMENSION, MacroCategory.LIPS, MacroCategory.NAILS)
    val diverseSet = mutableListOf<CandidateProvenance>()
    diverseSet.addAll(anchoredProv)

    for (category in targetCategories) {
        val categoryMatches = rankedRemaining.filter { it.cosmeticItem?.macroCategory == category }
        diverseSet.addAll(categoryMatches.take(2))
    }

    for (item in rankedRemaining) {
        if (diverseSet.size >= limit) break
        if (item !in diverseSet) {
            diverseSet.add(item)
        }
    }

    return diverseSet.take(limit)
}
```

In `StyleSimulatorEngine.kt`:
```kotlin
// Retrieve pre-scored cosmetic candidate provenance directly
val cCandidatesProv = candidateFilter.getCosmeticCandidateProvenance(cosmetics, context, limit = currentK)
val cCandidates = cCandidatesProv.mapNotNull { it.cosmeticItem }
```

---

### 2. Weather Telemetry Formatting Fix
**File**: [`PromptAssembler.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt)

```kotlin
val weatherContext = if (context.weather.contains("Temp:", ignoreCase = true)) {
    context.weather
} else {
    "${context.weather} (Temp: ${context.weatherTempC}°C, UV: ${context.uvIndex})"
}
```

---

### 3. Domain Role Alignment (`CosmeticRole`)
**File**: [`CosmeticRole.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/CosmeticRole.kt)

```kotlin
package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.MacroCategory

enum class CosmeticRole(val displayName: String) {
    EYE("Eye"),
    CHEEK("Cheek"),
    LIP("Lip"),
    NAIL("Nail"),
    PREP("Prep");

    companion object {
        fun fromMacroCategory(macroCategory: MacroCategory): CosmeticRole {
            return when (macroCategory) {
                MacroCategory.EYES -> EYE
                MacroCategory.DIMENSION -> CHEEK
                MacroCategory.LIPS -> LIP
                MacroCategory.NAILS -> NAIL
                else -> PREP
            }
        }
    }
}
```

---

## 3. Implementation Code Updates

Let's apply these code changes now!
