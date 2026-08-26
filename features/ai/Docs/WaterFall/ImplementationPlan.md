# Implementation Plan: AI Waterfall Engine & Token Optimization

This document details the step-by-step implementation for the **Provider-Aware AI Waterfall Engine** and the **Token Optimization Subsystem** for KoColor.

## Architectural Goal
Transition from a static AI request model to a dynamic "Retrieval-Reasoning" pipeline that maximizes local intelligence and minimizes cloud token consumption through Local RAG and semantic compression.

### The 85/15 Cognitive Split
The architecture explicitly acknowledges that deterministic tasks (weather gating, rotation penalties, availability checks) should be handled locally (85% of cognitive load), leaving only high-level aesthetic reasoning (15%) to the LLM.

---

## Phase 1: Policy & Budget Models

### [NEW] `TokenBudgetPolicy.kt`
- **Location**: `features:ai:local` (Core policy)
- **Goal**: Define the constraints for each AI execution tier.
- **Implementation**:
    - `AiProviderTier` enum: `LOCAL_NANO`, `FIREBASE_AI_LOGIC`, `BYOK_CLOUD`, `DETERMINISTIC_FALLBACK`.
    - `BudgetConfig` data class:
        - `maxInputTokens: Int`
        - `maxOutputTokens: Int`
        - `timeoutMillis: Long`
        - `topKCandidates: Int` (The number of wardrobe items to send).
    - **Policy Matrix**:
        - `LOCAL_NANO`: 768 in / 256 out / 8 items / 1200ms.
        - `FIREBASE_AI_LOGIC`: 1536 in / 512 out / 12 items / 3000ms.
        - `BYOK_CLOUD`: 4096 in / 1024 out / 16 items / 5000ms.

---

## Phase 2: Local RAG (Candidate Retrieval)

### [NEW] `WardrobeCandidateFilter.kt`
- **Location**: `applications:kocolor:data`
- **Goal**: Deterministically reduce $N=300$ items to a Top-$K$ pool of $8\text{--}16$ items.
- **Workflow**:
    - **Stage 1: Hard Pruning**:
        - Category mismatch (e.g., Hiking intent filters for activewear).
        - Weather gating (Temperature vs. Garment weight).
        - Rotation penalty (Exclude if worn in last 3 days).
    - **Stage 2: Soft Scoring**:
        - Match against `AppearanceTelemetry` (Undertone/Contrast).
        - Keyword matching against `userIntent`.
    - **Stage 3: Top-K Isolation**:
        - Sort by score and truncate based on the `topKCandidates` defined in the current `BudgetConfig`.

---

## Phase 3: Semantic Minification

### [NEW] `CompactManifestSerializer.kt`
- **Location**: `applications:kocolor:data`
- **Goal**: Convert wardrobe objects into dense, low-token strings.
- **Format**: `[id|category|name|hex|temperature|depth|material]`
- **Impact**: Reduces token footprint per item from ~120 (JSON) to ~20 (Compact Tuple).
- **Example**: `[w55|Top|Khaki Trench|#B8A992|Warm|Deep|Cotton]`

---

## Phase 4: Waterfall Engine Refactor

### [MODIFY] `StyleSimulatorEngine.kt`
- **Location**: `applications:kocolor:data`
- **Goal**: Orchestrate the flow and manage provider-aware execution.
- **Workflow**:
    1. **Tier Detection**: Check availability (Nano $\to$ BYOK $\to$ Firebase $\to$ Fallback).
    2. **Retrieval**: Call `WardrobeCandidateFilter` with the tier's $K$ limit.
    3. **Compression**: Call `CompactManifestSerializer`.
    4. **Cache Check**: Use existing SHA-256 fingerprint logic (Updated to include `tier` and `policyVersion`).
    5. **Preflight**: Verify `countTokens(manifest)` fits within `maxInputTokens`.
    6. **Execution**: Call provider with provider-specific timeout.
    7. **Fallback**: 
        - **Explicit Overflow Handling**: Specifically catch `QuotaExceededError` or `DOMException` for Gemini Nano if the 1024-token context limit is hit.
        - If any primary tier fails or times out, immediately trigger the next tier in the waterfall.

### [MODIFY] UI / ViewModel
- **TTFT Latency**: Implement granular loading states (e.g., "Warming up on-device engine...") to handle Time to First Token delays during local inference.

---

## Phase 5: Telemetry & Verification

### [MODIFY] Telemetry Logging
- Update `KoColor_Telemetry` to include:
    - `execution_tier_used: String` (e.g., "LOCAL_NANO", "FIREBASE_AI_LOGIC")
    - `retrieval_k_limit: Int`
    - `pruning_ratio: Float`
    - `serialization_strategy: String` ("JSON" vs "CompactTuple")
    - `latency_ms: Long`

---

## Verification Plan

### 1. Token Efficiency Audit
- Compare token usage of the "Compact Manifest" vs the previous "Minified JSON".
- Assert that `candidates_sent` never exceeds the budget defined in `TokenBudgetPolicy`.

### 2. Waterfall Resilience
- Simulate a network timeout (3000ms+) and verify the engine cascades from Tier 0 to Tier 1.5 without UI freezing.
- Verify that a `TokenLimitExceeded` event correctly skips the cloud tier.
- **Fault Injection**: Trigger a `QuotaExceededError` on Nano and verify the cascade to Tier 2 heuristics.

### 3. Local RAG Accuracy
- Verify that the Top-$K$ items selected locally are actually relevant to the `userIntent` and `weatherContext`.

### 4. Security & Production
- **App Check Replay Protection**: Ensure Replay Protection is enabled in the Firebase Console for the AI Logic API to ensure one-time-use tokens.
