# Implementation Plan: Secure Firebase AI Logic Integration for KoColor

This document outlines the architectural wiring to connect the `:features:ai:firebase` module into the KoColor styling pipeline, establishing an enterprise-grade, privacy-first AI execution environment.

## Architectural Requirements & Privacy Invariants

> [!CAUTION]
> **Absolute Privacy Invariant**: Raw biometric data (CameraX bitmaps, image URIs, or pixel arrays) must **NEVER** leave the device. All cloud-based AI requests are strictly limited to derived mathematical telemetry.
> **Backend Nomenclature**: This architecture uses **Firebase AI Logic** as the secure proxy gateway. All requests are attested via Firebase App Check with Limited-Use tokens.

### The Privacy Pipeline
1. **Acquisition**: Local CameraX capture.
2. **Analysis**: Local ML Kit Face Detection & Luminance Sampling.
3. **Extraction**: Generation of a structured `BiometricTelemetry` payload.
4. **Transmission**: Structured JSON telemetry sent to **Firebase AI Logic**.

---

## Proposed Changes

### [Core Data]

#### [AiConfigurationSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/AiConfigurationSettings.kt)
- **Developer Toggle**: `useFirebaseAiLogic: Flow<Boolean>` added for internal debug routing. 
- In production builds, this defaults to `true` and is hidden from standard settings.

### [KoColor Data & Use Cases]

#### [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- **Cascading Execution Logic**:
    - **Tier 0 (Firebase AI Logic)**: Primary secure route. Uses `FirebaseAiClient` with App Check attestation.
    - **Tier 1.5 (Local Gemini Nano)**: Fallback for offline or network failure.
    - **Tier 2 (Deterministic Heuristics)**: Final safety net.
- **Structured Prompting**: Accepts `telemetryJson: String` instead of raw strings to ensure LLM determinism.

### [KoColor Analyzer Feature]

#### [StyleSimulatorViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
- **Telemetry Structuring**: Implements `getStructuredTelemetry(telemetry: FaceTelemetryData): String`.
- Converts raw measurements into programmatic JSON:
  ```json
  { "biometrics": { "temperature": "warm", "depth": "light", "contrast": "balanced" } }
  ```
- Pass the structured payload and developer toggle state to the Engine.

### [Settings & UI]
- **Relocation**: Move the "Enterprise Security (Firebase)" toggle from standard AI Configuration to a hidden **Developer Menu**.

---

## Expanded Verification Matrix

### 1. Security & Privacy Audit
- **Payload Inspection**: Verify via proxy/logs that no multipart image data or Base64 blobs are transmitted.
- **Key Safety**: Assert that no Gemini API keys are present in Logcat or obfuscated strings.
- **Auth Enforcement**: Confirm requests are rejected if the Firebase user is not authenticated (if Auth mode is enabled).

### 2. App Check & Infrastructure
- **Token Lifecycle**: Validate that `useLimitedUseAppCheckTokens = true` is active and tokens are consumed correctly.
- **Debug Token**: Verify the local debug secret is generated and allows successful console registration.

### 3. Resilience (Fault Injection)
- **Network Failure**: Simulate `FirebaseAIException` and verify immediate fallback to Tier 1.5 (Local Nano).
- **Service Unavailability**: Disable Local AI and verify Tier 2 (Heuristics) produces a valid Style Blueprint.
- **Concurrency**: Ensure multiple rapid simulation triggers are handled without state corruption.
