# Implementation Plan: Secure Firebase AI Logic Integration for KoColor

This document outlines the architectural wiring to connect the `:features:ai:firebase` module into the KoColor styling pipeline, establishing an enterprise-grade, privacy-first AI execution environment.

## Architectural Requirements & Privacy Invariants

> [!CAUTION]
> **Absolute Privacy Invariant**: Raw camera imagery may be processed by on-device components, including Gemini Nano, but CameraX bitmaps, image URIs, pixel arrays, encoded images, and other raw image representations must **NEVER** leave the device. Only derived `StyleTelemetry` is transmitted to Firebase AI Logic. Raw camera imagery is never included in cloud requests.
> **Backend Nomenclature**: This architecture uses **Firebase AI Logic** as the secure proxy gateway. All requests are attested via Firebase App Check with Limited-Use tokens.

### The Bifurcated Data Flow
The architecture maximizes AI capability without compromising the network boundary by bifurcating the payload based on the execution environment:

1. **Tier 1.5 (Local Gemini Nano)**: Process rich visual data safely on-device.
   - **Payload**: Raw Image Bitmap + `StyleTelemetry`.
   - **Capability**: Offline multimodal reasoning.
2. **Tier 0 (Firebase AI Logic)**: Secure cloud execution over restricted mathematical data.
   - **Payload**: `StyleTelemetry` (JSON) only.
   - **Capability**: Enterprise-grade, attested cloud routing.

---

## Proposed Changes

### [Core Data]

#### [AiConfigurationSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/data/src/main/java/com/zoewave/probase/core/data/repository/AiConfigurationSettings.kt)
- **Developer Toggle**: `useFirebaseAiLogic: Flow<Boolean>` added for internal debug routing. 
- In production builds, this defaults to `true` and is hidden from standard settings.

### [KoColor Data & Use Cases]

#### [StyleSimulatorEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt)
- **Cascading execution with Type-Safe Boundaries**:
    - **Tier 0 (Cloud)**: `suspend fun getCloudAdvice(telemetry: StyleTelemetry): Blueprint`
    - **Tier 1.5 (Local)**: `suspend fun getLocalAdvice(image: Bitmap, telemetry: StyleTelemetry): Blueprint`
    - **Tier 2 (Heuristics)**: `fun getDeterministicAdvice(telemetry: StyleTelemetry): Blueprint`
- **Enforced Boundary**: The compiler prevents raw imagery from being passed into the `FirebaseAiClient`.

### [KoColor Analyzer Feature]

#### [StyleSimulatorViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)
- **Telemetry Structuring**: Implements mapping from local analysis into the type-safe `Appearance` and `StyleTelemetry` models.
- **Data Coordination**: Passes both the raw bitmap (for local tiers) and the structured telemetry to the engine.

### [Settings & UI]
- **Relocation**: Move the "Enterprise Security (Firebase)" toggle from standard AI Configuration to a hidden **Developer Menu**.

---

## Expanded Verification Matrix

### 1. Security & Privacy Audit
- **Payload Inspection**: Verify via proxy/logs that no multipart image data or Base64 blobs are transmitted in cloud requests.
- **Key Safety**: Assert that no Gemini API keys are present in Logcat or obfuscated strings.
- **Auth Enforcement**: Confirm requests are rejected if the Firebase user is not authenticated.

### 2. App Check & Infrastructure
- **Token Lifecycle**: Validate that `useLimitedUseAppCheckTokens = true` is active and tokens are consumed correctly.
- **Debug Token**: Verify the local debug secret is generated and allows successful console registration.

### 3. Resilience (Fault Injection)
- **Network Failure**: Simulate `FirebaseAIException` and verify immediate fallback to Tier 1.5 (Local Nano) with image ingestion.
- **Service Unavailability**: Disable Local AI and verify Tier 2 (Heuristics) produces a valid Style Blueprint based strictly on telemetry.
