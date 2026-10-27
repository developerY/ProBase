# ProBase Shared AI Feature Module Documentation

Welcome to the central documentation repository for the `:features:ai` module. This module provides a completely agnostic, secure, and extensible architecture for routing multimodal intelligence requests across the ProBase monorepo (KoColor, Seaweed, PhotoDo, GoSwift).

## Directory Structure

*   **`architecture/`**: High-level system designs, routing architectures, configuration decoupling, and testing structures.
    *   `AI_Optimization_Walkthrough.md`
    *   `Capability_Router_Architecture_Walkthrough.md`
    *   `Retrieval_Reasoning_Pipeline_Capability_Router.md`
    *   `ai_configuration_isolation.md`
    *   `local_ai_architecture.md`
    *   `shared_ai_telemetry_architecture.md` (Implementation guide for AppDiagnosticDelegate and unified debug testing)
*   **`integration/`**: Step-by-step guides for connecting specific API keys, App Check tokens, and secure SDK boundaries.
    *   `firebase_api_key_integration.md`
    *   `secure_vertex_ai_integration.md`
*   **`domain/`**: Specific engine definitions and how the pipeline executes strict parsing across apps.
    *   `computational_styling_engine.md` (KoColor styling logic mapped onto the AI engine)
*   **`archive/`**: Contains previous hackathon drafts, iterative development steps, testing scratchpads, and historical planning notes.
