# Implementation Plan: Token Optimization, Local RAG & Semantic AI Caching

This document details the architectural plan to optimize token usage and network efficiency for the KoColor AI pipeline, transitioning to a scalable Local RAG (Retrieval-Augmented Generation) pattern.

## Objectives
- **Reduce Costs**: Minimize prompt tokens sent to Gemini.
- **Improve Latency**: Faster responses via pre-filtering and semantic caching.
- **Privacy First**: Maintain mathematical telemetry boundaries while allowing rich local analysis.
- **Observability**: Rigorous tracking of token budgets, fallback reasons, and execution tiers.

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
  - **Rotation Penalty**: Exclude items where `RotationPenalty >= MAX_ROTATION_PENALTY` (default `0.70`). This is a retrieval policy, not an AI rule.
  - **Context Suitability**: Apply deterministic filters for weather (e.g., no parkas in 30°C), color compatibility, and **user intent / occasion**.
- **Semantic Minification**:
  - Map items to a compact representation: `[ID, SemanticType, HexColor]`.
  - **CRITICAL**: Retain stable database IDs (e.g., `w_55`) for UI hydration.
  - Drop database-only metadata, timestamps, and marketing descriptions.
  - *Example*: `["w_55", "trench coat", "#B8A992"]`

---

## Phase 3: Deterministic AI Result Caching

### [AI Orchestration Layer]

#### [PromptCacheRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/data/PromptCacheRepository.kt)
> [!NOTE]
> Relocated to the local AI module to serve as a provider-agnostic caching layer for all execution tiers (Firebase and Nano).

- **Execution-Aware Fingerprinting**: Generate a `SHA-256` hash to memoize deterministic `StyleBlueprint` results. The fingerprint includes the intended execution tier to prevent cross-tier result collision.
  ```text
  SHA-256(
      executionTier +
      promptVersion +
      modelVersion +
      retrievalPolicyVersion +
      appearanceTelemetry +
      weatherState +
      userIntent +
      minifiedManifest
  )
  ```
- **Storage & Policy**: 
  - Bounded `LruCache` with configurable maximum entries.
  - Least-recently-used entries are evicted automatically to manage memory.
  - A change to any fingerprint input produces a **cache miss** and triggers a new AI request.
  - **NO raw images** are ever stored in the cache; only the derived blueprint and metadata.

#### [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- **Multi-Tier Cache Check**: 
  - Check for a **Tier 0 (Cloud)** cached result first.
  - If Tier 0 execution is skipped (budget) or fails, check for a **Tier 1.5 (Nano)** cached result before executing local inference.
- **Skip AI**: If a valid result is found for the active tier, return it with no AI cost and near-instant local retrieval.

---

## Phase 4: Token Budgeting & Telemetry

### [AI / Engine Layer]

#### [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- **Token Preflight**: Use `model.countTokens(prompt)` on the **exact model/configuration** that will execute to estimate usage.
- **Circuit Breaker**: If `estimatedInputTokens > MAX_CLOUD_INPUT_TOKENS` (default `4,000`), **skip Cloud Tier 0 and route directly to Tier 1.5 (Local Gemini Nano)** to protect billing.

### [Observability]

#### **Telemetry Logs** (`KoColor_Telemetry`)
Upon each request, log a comprehensive metric set:
```text
- cache_hit: Boolean
- cache_key: String (Shortened, non-sensitive hash identifier)
- vault_size: Int
- eligible_count: Int
- candidates_sent: Int
- estimated_input_tokens: Int
- actual_prompt_tokens: Int
- completion_tokens: Int
- total_tokens: Int
- execution_tier: String (Tier 0 | Tier 1.5 | Tier 2)
- fallback_reason: String? (e.g., "TOKEN_BUDGET", "NETWORK_FAILURE")
- model: String
- prompt_version: String
- retrieval_policy_version: String
```
**Privacy Note**: Never include raw prompt text or full telemetry payloads in production logs.

---

## Verification Plan

1. **Token Audit**: Run simulation with full wardrobe and verify `candidates_sent` is a focused subset of `vault_size`.
2. **Network Audit**: Inspect Logcat for `OpenMeteo` responses to verify `forecast_days=1` is applied.
3. **Cache Test**: Trigger same intent twice within stable weather and verify `cache_hit: true` and near-instant local retrieval.
4. **Budget Test**: Inject a fake massive manifest and verify the system correctly **skips Cloud Tier 0** and logs `fallback_reason=TOKEN_BUDGET` when exceeding 4,000 tokens.
5. **Fingerprint Test**: Change the `retrievalPolicyVersion` in code and verify the cache is correctly bypassed.
