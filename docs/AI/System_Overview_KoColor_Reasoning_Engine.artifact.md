# Architectural Overview: The KoColor Reasoning Engine

This document provides a high-level executive summary of the sophisticated AI infrastructure built for KoColor. The system has evolved from a basic "API-wrapper" into a production-grade **Retrieval-Reasoning Pipeline** that is privacy-hardened, hardware-optimized, and cost-efficient.

---

## 1. Core Architecture: The "85/15" Local-First Split
The engine enforces a strict division of cognitive labor to maximize performance and minimize token waste:
*   **Local Retrieval (85%):** Deterministic Kotlin code handles database lookups, availability checks (including `isHidden`), weather-category gating (e.g., no parkas in 30°C), and rotation penalties (with a 3-day hardware-fallback).
*   **Generative Reasoning (15%):** AI is reserved only for high-level coordination—matching the user's intent with a pre-qualified shortlist of candidates to create a harmonic style blueprint.

---

## 2. The Dynamic Capability Router
Instead of a fixed sequence, the system detects and ranks AI backends dynamically based on current hardware and network states:
1.  **Tier 1.5 (Local NPU):** Priority 1. Uses private on-device silicon (Gemini Nano) for instant, free reasoning.
2.  **Tier 0 (Firebase AI):** Priority 2. Secure, high-intelligence cloud reasoning protected by **App Check (Play Integrity)** and Anonymous Auth.
3.  **Tier 1 (BYOK):** Priority 3. Deep fallback for user-provided API keys.
4.  **Tier 2 (Heuristic Engine):** The indestructible baseline. Ensures a styling result is provided even if completely offline or without AI models.

---

## 3. Adaptive Token Optimization Subsystem
To protect your $1,000 credit budget and ensure compatibility with small-context models, we implemented an **Adaptive Fit Loop**:
*   **Semantic Minification:** Slashing item token costs by **~80%** by replacing verbose JSON with dense tuples (e.g., `[id|category|name|hex|temp|depth|material]`).
*   **Step-Down Fitting:** If a request exceeds a provider's budget, the engine automatically:
    1.  Strips non-essential metadata fields (Expanded $\to$ Balanced $\to$ Minimal).
    2.  Reduces the candidate count (Top-$K$) in steps of 2.
    3.  Re-calculates exact token counts until the request fits or failover occurs.

---

## 4. Privacy & Security Invariants
*   **Type-Safe Privacy Boundary:** Enforced at the compiler level. Cloud providers strictly receive mathematical telemetry (`StyleTelemetry`). Transmission of raw bitmaps, URIs, or pixels to the cloud is impossible.
*   **On-Device Multimodal:** Because local models stay on-NPU, they are permitted to ingest raw portrait images for rich visual reasoning without a privacy compromise.
*   **Secure Session Management:** Authentication and session state are encapsulated within the providers, keeping the UI/ViewModel layers clean and logic-free.

---

## 5. Performance & Observability
*   **Multi-Tier Deterministic Caching:** SHA-256 fingerprinting that includes the `executionTier`, ensuring cached results are contextually accurate and preventing tier collisions.
*   **Structured Telemetry (`KoColor_Telemetry`):** A granular Logcat audit trail for every request, tracking:
    *   Execution Tier & Cache Hits
    *   Retrieval Limits (K) & Serialization Strategy
    *   Exact Tokens Used & Latency (ms)
    *   Fallback/Failure Reasons

---

## 6. Implementation Health
*   **Unit Tests:** 100% pass rate for core engine logic, including the fit-loop, candidate filtering, and minification.
*   **Build Status:** Fully assembled and ready for deployment to flagship test devices.

---

> [!IMPORTANT]
> This system is built to scale. To target future high-token local models or new cloud providers, you simply implement the `AiProvider` interface—the engine handles the rest.
