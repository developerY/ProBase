# Implementation Plan: AI-First Product Discovery Pipeline

Implement a local-first, cloud-enriched discovery pipeline utilizing Gemini Nano (local), Room 3 (persistence), and Cloud Gemini (WorkManager enrichment).

## Proposed Changes

### 1. Local AI Refinement (`:features:ai:local`)
- Update `LocalAiEngine.kt` to implement the `NanoState` machine.
- Enhance `LocalStandardizedData` schema to include `claims` and `directions`.
- Implement safe JSON extraction via Regex to bypass LLM markdown artifacts.

### 2. Data Layer Evolution (Room 3 & WorkManager)
- Define `ProductEntity` and `ProductResolution` in the domain layer.
- Set up Room 3 (`androidx.room3`) database infrastructure.
- Implement `@ColumnTypeConverter` utilizing `kotlinx.serialization` for complex types.
- Create `EnrichmentWorker.kt` for background Web Gemini processing.

### 3. Orchestration & Use Cases
- Implement `ResolveProductUseCase.kt` to merge Local AI output with opportunistic API data.
- Implement deterministic confidence calculation (ParsingMetrics).

### 4. UI State Integration
- Update `BoxCaptureViewModel` to drive the `DiscoveryState` state machine.
- Refactor UI components to handle `EnrichmentDeferred` and `FullyEnriched` states.

## Verification Plan

### Automated Tests
- `LocalAiEngineTest`: Verify Regex-based JSON extraction.
- `ResolveProductUseCaseTest`: Verify confidence score weighting.
- `Room3ConverterTest`: Ensure serialization/deserialization of lists and enums.

### Manual Verification
- Deploy to device and verify "Local AI" spinner on Discovery Health screen.
- Verify WorkManager triggers Cloud Gemini enrichment when network becomes available.
- Inspect Room database using App Inspection to verify standardized data persistence.
