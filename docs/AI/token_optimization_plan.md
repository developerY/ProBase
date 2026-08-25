# Implementation Plan: Token Optimization & Local RAG

This document details the architectural plan to optimize token usage and network efficiency for the KoColor AI pipeline, transitioning to a scalable Local RAG (Retrieval-Augmented Generation) pattern.

## Objectives
- **Reduce Costs**: Minimize prompt tokens sent to Gemini.
- **Improve Latency**: Faster responses via pre-filtering and semantic caching.
- **Privacy First**: Maintain mathematical telemetry boundaries while allowing rich local analysis.
- **Observability**: Rigorous tracking of token budgets and execution tiers.

---

## Phase 1: Network & Environmental Optimization

### [Core Network]

#### [OpenMeteoService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/network/src/main/java/com/zoewave/probase/core/network/api/interfaces/OpenMeteoService.kt)
- **Update Query**: Add `@Query("forecast_days") days: Int = 1` to `getEnvironmentalContext`.
- **Impact**: Reduces UV forecast payload from 7 days (~4.6KB) to 1 day (~800B).

### [Core Data]

#### [AtmosphericRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/weather/AtmosphericRepository.kt)
- **Enforce TTL**: Ensure `fetchWeatherIfNeeded` is the primary entry point for ViewModels with a 15-minute TTL.
- **Cache Integrity**: Verify that `forceRefresh` is only triggered by explicit user "Pull-to-Refresh" actions.

---

## Phase 2: Local RAG (Candidate Retrieval)

### [KoColor Data]

#### [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- **Local Retrieval Pipeline**: Before building the manifest, candidates are filtered through multiple local dimensions:
  - **Category Eligibility**: Entirely strip noise categories (`Oral`, `Tools`, `Fragrance`, `Grooming`, `Organizers`).
  - **Rotation Penalty**: Exclude items with `RotationPenalty >= 0.70`.
  - **Context Suitability**: Apply deterministic filters for weather (e.g., no parkas in 30°C) and color compatibility.
- **Semantic Minification**:
  - Map items to a compact representation: `[ID, SemanticType, HexColor]`.
  - **CRITICAL**: Retain stable database IDs (e.g., `w_55`) for UI hydration.
  - Drop database-only metadata, timestamps, and marketing descriptions.
  - *Example*: `["w_55", "trench coat", "#B8A992"]`

---

## Phase 3: Deterministic Prompt Caching

### [AI Feature Module]

#### [PromptCacheRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/PromptCacheRepository.kt) [NEW]
- **Semantic Fingerprinting**: Generate a `SHA-256` hash to memoize responses.
  ```text
  SHA-256(
      promptVersion +
      modelVersion +
      appearanceTelemetry +
      weatherState +
      userIntent +
      minifiedManifest
  )
  ```
- **Storage**: Use an in-memory `LruCache` for near-instant local retrieval of repeated simulations. The cache is invalidated if any input parameter (including weather) changes the fingerprint.

#### [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- **Check Cache**: Before Tier 0 (Firebase) execution, consult `PromptCacheRepository`.
- **Skip Network**: If hit, return cached `StyleBlueprint` with no cloud AI cost.

---

## Phase 4: Token Budgeting & Telemetry

### [AI / Engine Layer]

#### [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- **Token Preflight**: Use `model.countTokens(prompt)` to estimate usage before sending.
- **Circuit Breaker**: If `estimatedInputTokens > 3000`, cancel Cloud Tier 0 and force Fallback to Tier 1.5 (Local Gemini Nano) to protect billing.

### [Observability]

#### **Telemetry Logs** (`KoColor_Telemetry`)
Upon each request, log a comprehensive metric set:
```text
- cache_hit: Boolean
- vault_size: Int
- eligible_count: Int
- candidates_sent: Int
- estimated_input_tokens: Int
- actual_prompt_tokens: Int
- completion_tokens: Int
- total_tokens: Int
- execution_tier: String (Tier 0 | Tier 1.5 | Tier 2)
- model: String
- prompt_version: String
```

---

## Verification Plan

1. **Token Audit**: Run simulation with full wardrobe and verify `candidates_sent` is a focused subset of `vault_size`.
2. **Network Audit**: Inspect Logcat for `OpenMeteo` responses to verify `forecast_days=1` is applied.
3. **Cache Test**: Trigger same intent twice within stable weather and verify `cache_hit: true` and zero network activity.
4. **Budget Test**: Inject a fake massive manifest and verify the system correctly trips the 3,000 token circuit breaker.
5. **Fingerprint Test**: Change the `promptVersion` in code and verify the cache is correctly bypassed.
