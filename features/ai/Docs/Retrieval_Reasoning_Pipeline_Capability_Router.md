# Retrieval-Reasoning Pipeline & Capability Router

This document serves as the formal architectural reference for the KoColor **Retrieval-Reasoning Pipeline**. This system transitions KoColor from a "Chat-with-Database" model to a sophisticated on-device intelligence architecture that treats Generative AI as a high-level coordination layer.

---

## 1. Architectural Philosophy: The 85/15 Split

We enforce a strict separation between **Deterministic Retrieval** and **Generative Reasoning**. This ensures maximum performance, privacy, and token efficiency.

*   **85% Local Deterministic (The "Retrieval" Phase):** 
    Database queries, weather-gating, laundry status, and rotation scoring are handled entirely by on-device Kotlin code.
*   **15% Generative Reasoning (The "Reasoning" Phase):** 
    The AI is only invoked to solve the "aesthetic coordination problem"—constructing a harmonic style from a pre-qualified shortlist of candidates.

---

## 2. Component Ecosystem

### A. Wardrobe Candidate Filter (Local RAG)
The [`WardrobeCandidateFilter`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt) is the workhorse of the retrieval phase. It prunes the inventory ($N=300+$ items) into a "Reasoning Set" ($K=8\text{--}16$ items).
-   **Hard Pruning:** Immediately drops items failing availability (including `isHidden`), temperature heuristics (e.g., no `OUTERWEAR` in heat), or rotation constraints.
-   **Rotation Fallback:** Automatically excludes items worn in the last 3 days if projected rotation scores are unavailable.
-   **Soft Scoring:** Ranks items based on matching user `AppearanceTelemetry` and intent.

### B. Compact Manifest Serializer
The [`CompactManifestSerializer`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/applications/kocolor/data/usecase/CompactManifestSerializer.kt) converts rich domain objects into dense, low-token tuples.
-   **Impact:** Reduces item-level token cost by **~80%**.
-   **Detail Levels:** Supports `MINIMAL`, `BALANCED`, and `EXPANDED` metadata sets.

### C. Capability Router
The [`CapabilityRouter`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/AiRoutingImpl.kt) dynamically detects and ranks available AI backends on the device.
1.  **Tier 1.5 (Local Nano):** Highest priority. Runs on-device NPU. Private and free.
2.  **Tier 0 (Firebase AI):** Secondary priority. High-intelligence managed cloud. Encapsulates anonymous authentication.
3.  **Tier 1 (BYOK):** Fallback cloud tier for user-provided keys.

---

## 3. The Adaptive Fitting Loop (The "Step-Down" Strategy)

The [`StyleSimulatorEngine`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt) performs a **Preflight Token Audit** before every request. If the prompt exceeds the provider's `maxInputTokens`, it adaptively compresses the context:

1.  **Assemble Prompt:** Build the exact final prompt string.
2.  **Audit Tokens:** Call `provider.countTokens()`.
3.  **Step-Down Logic:**
    -   **Attempt A:** Downgrade `EXPANDED` manifest to `BALANCED`.
    -   **Attempt B:** Downgrade `BALANCED` to `MINIMAL`.
    -   **Attempt C:** Reduce `Top-K` candidate count (e.g., $16 \to 14 \to 12$).
    -   **Attempt D:** Failover to the next best provider.

---

## 4. Security & Privacy Invariants

### Type-Safe Privacy Boundary
The system enforces privacy at the architectural level using the `AiInput` sealed interface:
-   **Compile-Time Type Safety:** Cloud providers strictly accept `AiInput.TextOnly`. Raw images are encapsulated in `AiInput.Multimodal` and are only constructible for providers with `supportsLocalImageIngestion = true`.
-   **Telemetry Only:** Cloud prompts strictly receive `ColorTelemetry` / `AppearanceProfile` vectors and compact text manifests.

### Deterministic Multi-Tier Caching
Results are cached using a SHA-256 fingerprint that includes the `executionTier`. This ensures that a local NPU result is never reused as a cloud result, maintaining execution integrity across tiers.

---

## 5. Observability: `KoColor_Telemetry`

Every execution event is logged with precise metrics to audit the reasoning pipeline:
-   `execution_tier_used`: The specific model or cache source.
-   `retrieval_k_limit`: The number of items seen by the AI.
-   `serialization_strategy`: The detail level used (Minimal/Balanced/Expanded).
-   `latency_ms`: Exact performance timing.
-   `tokens_used`: Exact prompt/completion token accounting.

---

## 6. Maintenance & Extensibility

To add a new AI model:
1.  Implement the [`AiProvider`](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/AiProvider.kt) interface.
2.  Define its [`AiProviderCapability`](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/AiProvider.kt) metadata.
3.  Register it in the `CapabilityRouterImpl`.

The engine will automatically handle the context fitting, RAG retrieval, and fallback logic for the new provider.
