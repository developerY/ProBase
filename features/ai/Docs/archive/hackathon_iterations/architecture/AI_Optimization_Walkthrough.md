# Technical Walkthrough: Secure & Optimized AI Pipeline

This document provides a comprehensive summary of the architectural overhaul performed on the KoColor AI styling pipeline. We have transitioned from a simple cloud-request model to a sophisticated, multi-tiered, privacy-first execution environment.

## 1. The Multi-Tier "Waterfall" Architecture
The core of the system is the `StyleSimulatorEngine`, which now manages a cascading fallback logic to balance intelligence, cost, and availability.

| Tier | Provider | Data Payload | Best For |
| :--- | :--- | :--- | :--- |
| **Cache** | Local LRU | N/A (Fingerprint) | Instant hits ($0) |
| **Tier 0** | Firebase AI Logic | **Telemetry Only** | Enterprise Reasoning |
| **Tier 1.5** | Gemini Nano | **Image + Telemetry** | Private Multimodal |
| **Tier 1** | BYOK (Google AI) | Telemetry Only | Deep Fallback |
| **Tier 2** | Local Heuristics | Metadata Only | Offline / No AI |

---

## 2. Production Security & Authentication
To protect our API quota and ensure secure routing, we implemented a layered defense:

*   **Firebase AI Logic Proxy**: Replaced the raw Google Generative AI SDK. Requests are now routed through Firebase's secure gateway using the public Firebase API key.
*   **App Check Enforcement**:
    *   **Production**: Enforces Play Integrity to ensure only genuine app binaries access the AI.
    *   **Development**: Uses `DebugAppCheckProviderFactory` for local testing.
    *   **Replay Protection**: Enabled `useLimitedUseAppCheckTokens = true`.
*   **Anonymous Authentication**: Implemented `FirebaseAiAuthManager` to satisfy "Authenticated-Users Mode" without requiring a user login screen.

---

## 3. The Privacy Bypath (Data Bifurcation)
We established a non-negotiable privacy boundary enforced at the compiler level:

*   **Cloud Invariant**: Raw pixels (bitmaps/URIs) **never** leave the device. Cloud requests strictly receive a mathematical `StyleTelemetry` JSON (e.g., `Warm • Light • Balanced`).
*   **Local Multimodal**: Because **Gemini Nano** runs entirely on the device's silicon, we allowed it to ingest the raw portrait bitmap, unlocking rich visual reasoning without a privacy compromise.

---

## 4. Efficiency: Local RAG & Deterministic Caching
We significantly reduced token consumption and latency through on-device intelligence:

*   **Local Candidate Retrieval (RAG)**: The app now deterministicly filters the wardrobe *before* building the prompt:
    *   Prunes noise categories (Oral, Tools, etc.).
    *   Excludes high-rotation items (Penalty > 0.70).
    *   Filters by weather suitability (e.g., no parkas in 30°C).
    *   Filters by seasonal color profile.
*   **Semantic Minification**: Items are mapped to a compact `[ID, Name, Hex]` format, stripping database metadata.
*   **Deterministic AI Caching**:
    *   Implemented a SHA-256 fingerprinting system.
    *   Fingerprint includes: `Tier + Versions + Telemetry + Weather + Intent + Manifest`.
    *   Repeated simulations with the same context return instantly via the `PromptCacheRepository`.

---

## 5. Token Budgeting & Circuit Breaker
To protect the $1,000 credit budget, we implemented active monitoring:

*   **Token Preflight**: Every cloud request calls `countTokens()` before execution.
*   **The 4K Budget**: If the estimated prompt exceeds **4,000 tokens**, the system automatically skips Tier 0 and falls back to Tier 1.5 (Nano) or Tier 2 (Heuristics).

---

## 6. Observability: KoColor_Telemetry
We now log a rich metric set for every simulation event:
```text
- cache_hit: Boolean
- vault_size: total items in DB
- candidates_sent: items actually seen by AI
- execution_tier: Tier 0, 1.5, or 2
- tokens_actual: exact cost from metadata
- fallback_reason: TOKEN_BUDGET, NETWORK_FAILURE, etc.
```

> [!TIP]
> Filter Logcat by **`KoColor_Telemetry`** to see the real-time performance of this optimized pipeline.
