# Implementation Plan: Intent Anchor Terminology, Decompressed Candidate Scoring & Audit Fulfillment Logging

Based on the architectural review in `ArchRevG2.md`, this document details the implementation plan for correcting anchor terminology, decompressing secondary candidate scoring via dynamic CIELAB chroma scaling, and exposing **Intent Fulfillment** metrics in the audit trail.

---

## 1. Resolved Objectives & Architectural Tasks

| Task | Objective | Key Refinement |
| :--- | :--- | :--- |
| **1. Anchor Terminology Alignment** | Distinguish user locks from automatic intent overrides in audit logs. | If anchor is `USER_LOCKED`, use `[LOCKED ANCHOR]`. If anchor is `AUTOMATIC_CONTEXT` intent override, use `[INTENT ANCHOR] High-chroma intent override`. |
| **2. Decompressed Candidate Scoring** | Decompress candidate score rankings (`0.84–0.85`) when color-driven intent is active. | Scale candidate scores continuously using CIELAB Chroma ($C^*$): `score += (itemChroma / 50.0f) * HIGH_CHROMA_INTENT_BONUS`. |
| **3. Intent Fulfillment Audit Logging** | Expose quantitative request satisfaction metrics alongside FASHIONISTA aesthetic calibration. | Add `[6] INTENT FULFILLMENT` to `KOCOLOR AUDIT TRAIL` displaying Colorfulness, Color Contrast, Overall Intent Match score, and `unmetIntent` tags. |

---

## 2. Code Implementation Details

### Task 1: Anchor Terminology Alignment
**Files**: [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt) & [`DeterministicContextEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/DeterministicContextEngine.kt)

In `StyleSimulatorEngine.kt`:
```kotlin
val anchorProv = selectionState.activeAnchors.map { anchor ->
    val isUserLock = context.lockedConstraints.any {
        (it.itemId == "w_${anchor.internalId}" || it.itemId == anchor.remoteId) &&
        (it.tier == SelectionTier.LOCKED || it.tier == SelectionTier.FORCED)
    }
    val rationaleText = if (isUserLock) {
        "[LOCKED ANCHOR] Required outfit anchor"
    } else {
        "[INTENT ANCHOR] High-chroma intent override"
    }
    CandidateProvenance(
        clothingItem = anchor,
        contextScore = 1.0f,
        colorScore = 1.0f,
        appearanceScore = 1.0f,
        freshnessScore = 1.0f,
        retrievalReason = rationaleText
    )
}
```

---

### Task 2: Decompressed Candidate Scoring via CIELAB Chroma
**File**: [`WardrobeCandidateFilter.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt)

```kotlin
// In calculateScore(item, context):
val chroma = calculateChroma(item.colorHex)
if (context.intentProfile.colorfulness > 0.7f) {
    // Dynamic continuous scaling curve (e.g. C* = 40 -> +2.0f, C* = 60 -> +3.0f)
    val chromaScaledBonus = (chroma / 50.0f) * RecommendationWeights.HIGH_CHROMA_INTENT_BONUS
    if (chroma > 20.0f) {
        score += chromaScaledBonus
    } else {
        score += RecommendationWeights.MONOCHROME_NEUTRAL_PENALTY // -1.5f
    }
}
```

---

### Task 3: Intent Fulfillment Audit Trail Logging
**Files**: [`StyleAuditLog.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/telemetry/StyleAuditLog.kt), [`StyleAuditLogger.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/telemetry/StyleAuditLogger.kt), [`StyleSimulatorEngine.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)

In `StyleAuditLogger.kt`:
```kotlin
appendLine("[6] INTENT FULFILLMENT")
trail.intentFulfillment?.let { fulfillment ->
    appendLine("    Overall Intent Match: ${"%.1f".format(fulfillment.score)} / 100")
    appendLine("    Colorfulness Score: ${"%.2f".format(fulfillment.dimensions.colorfulness)}")
    appendLine("    Color Contrast: ${"%.2f".format(fulfillment.dimensions.colorContrast)}")
    appendLine("    Unmet Parameters: ${fulfillment.unmetIntent.ifEmpty { "None" }}")
} ?: appendLine("    NO INTENT FULFILLMENT RECORDED")
appendLine("==================================================")
```

In `StyleSimulatorEngine.kt`:
```kotlin
val intentFulfillment = intentFulfillmentEvaluator.evaluate(
    intentProfile = requestContext.intentProfile,
    selectedClothing = selectedClothingItems,
    selectedCosmetics = selectedCosmeticItems
)
auditLogger.logIntentFulfillment(requestContext.requestId, intentFulfillment)
```

---

## 3. Verification Plan

* **Unit Tests**: Run `:applications:kocolor:data:testDebugUnitTest` and `:applications:kocolor:features:analyzer:testDebugUnitTest`.
* **Build Verification**: Run `:applications:kocolor:apps:mobile:assembleDebug`.
* **Audit Inspection**: Trigger style simulation and verify steps `[5] AESTHETIC CALIBRATION (FASHIONISTA)` and `[6] INTENT FULFILLMENT` are logged cleanly in `KOCOLOR AUDIT TRAIL`.
