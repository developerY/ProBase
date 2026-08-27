# Technical Walkthrough: Dynamic Capability Router & Adaptive AI Pipeline

This document details the architectural evolution of the KoColor styling pipeline from a static "Waterfall" fallback model to a **Dynamic Capability-Aware Routing** system. This refactor standardizes how we interact with on-device and cloud AI, prioritizes local deterministic reasoning, and aggressively optimizes token consumption.

---

## 1. The Core Philosophy: "The Local-First Split"

We have decoupled the styling process into two distinct cognitive phases:

1.  **Deterministic Reasoning (85% of load):** Handled locally by `WardrobeCandidateFilter`. It performs database-driven tasks like weather gating, availability checks (including hidden status), and rotation penalties (with a 3-day hardware-clock fallback).
2.  **Generative Reasoning (15% of load):** Handled by an `AiProvider`. It solves the "aesthetic coordination problem" using only the pre-qualified candidates. Providers encapsulate their own session management, such as anonymous authentication for cloud tiers.

This ensures we never waste expensive LLM tokens on simple database queries (e.g., "Is this shirt in the laundry?").

---

## 2. Key Component Breakdown

### A. Provider Abstraction (`:features:ai:core`)
The new core module defines the unified contract for all AI backends.
*   **`AiProvider`**: A common interface for Gemini Nano (Local), Firebase AI (Cloud), and BYOK.
*   **`AiProviderCapability`**: A metadata model describing a provider's limits (Max Tokens, Timeout, Top-K capacity).
*   **`AiExecutionFailure`**: A unified sealed interface for error handling (ContextLimitExceeded, Timeout, etc.).
*   **Type-Safe Privacy Invariant**: Cloud provider interfaces are strictly typed to accept only `StyleTelemetry`, making the transmission of raw imagery architecturally impossible.

### B. Local Candidate RAG (`WardrobeCandidateFilter`)
A multi-stage pipeline that prunes $N=300+$ items down to a manageable Top-$K$ set:
1.  **Hard Pruning**: Drops items that are marked as `isHidden`, weather-incompatible (e.g., no heavy coats in 25°C+), or violated by rotation rules.
2.  **Soft Scoring**: Ranks remaining items against the user's `AppearanceTelemetry` and `userIntent` keywords.
3.  **Rotation Fallback**: If specific rotation scores are missing, the filter uses physical `lastUsedTimestamp` metadata to exclude items worn within the last 3 days.
4.  **Truncation**: Returns exactly the number of items allowed by the current execution tier.

### C. Semantic Minification (`CompactManifestSerializer`)
To slash token costs by **~80%**, we replaced verbose JSON with dense tuples:
*   **Expanded**: `[w55|Top|Khaki Trench|#B8A992|Warm|Deep|Cotton]`
*   **Minimal**: `[w55|Top|Khaki Trench|#B8A992]`
Every character in the prompt must now "earn its place."

---

## 3. The Adaptive "Step-Down" Fitting Loop

The `StyleSimulatorEngine` no longer just "sends a request." It adaptively fits the context to the selected provider's specific budget:

1.  **Select Provider**: The `CapabilityRouter` provides the best available backend (preferring Local Nano).
2.  **Preflight Check**: The engine assembles the **exact final prompt** and calls `countTokens()`.
3.  **Adaptive Trimming**: If the prompt is too large:
    *   **Step 1**: Downgrade metadata detail (`EXPANDED` → `BALANCED` → `MINIMAL`).
    *   **Step 2**: Reduce the candidate count (`Top-K` decreased by 2).
    *   **Step 3**: Re-count and repeat until it fits.
4.  **Execute or Failover**: If it fits, execute. If it exhausts the minimum useful context (`minTopK`), it fails over to the next provider in the ranked list.

---

## 4. Resilience & Fallback

*   **`HeuristicStyleEngine`**: An indestructible baseline that uses smart-randomization and basic color matching to provide a "styling blueprint" even when completely offline or without AI capacity.
*   **`ByokAiProvider`**: A "Bring Your Own Key" provider that acts as a deep fallback (Priority 3) when neither local nor managed cloud tiers are available.
*   **Encapsulated Auth**: Cloud providers like `FirebaseAiProvider` manage their own session state (e.g., `authManager.signInAnonymously()`) internally during execution, keeping the ViewModel layer clean.
*   **Unified Exceptions**: Because every provider maps its errors to `AiExecutionFailure`, the simulator engine can handle a network drop or a Nano context-limit error using the exact same logic.

---

## 5. Benefits Summary

| Metric | Previous (Static) | New (Adaptive) |
| :--- | :--- | :--- |
| **Token Cost** | ~1,200 tokens/request | **~300-500 tokens/request** |
| **Resilience** | Brittle Waterfall | **Dynamic Provider Re-routing** |
| **Privacy** | Telemetry Only (Cloud) | **Local Multimodal (Nano) + Telemetry (Cloud)** |
| **Maintenance** | Hardcoded Provider Logic | **Provider-Agnostic Core Interfaces** |

---

## 6. How to Extend
To add a new AI model (e.g., Gemini 1.5 Pro via BYOK):
1.  Create a class implementing `AiProvider`.
2.  Define its `AiProviderCapability` (e.g., `maxInputTokens = 128000`).
3.  Add it to the `CapabilityRouterImpl` ranking logic.
4.  The engine will automatically handle the context fitting and fallback for it.
