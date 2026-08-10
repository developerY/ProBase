# Implementation Plan: KoColor Distributed Static Backend & Taxonomy Alignment

This plan outlines the steps to build the `kocolor` backend using a distributed "Static-First" architecture and align the Android domain models and Room database with the **Glow Archive Taxonomy**.

## User Review Required

> [!IMPORTANT]
> **Taxonomy Alignment**: I will introduce the `Temperature` facet (Warm, Cool, Neutral, Olive) to the Android domain model and Room entity, as it is currently missing from `CosmeticItem.kt` but defined in the taxonomy documentation.

> [!NOTE]
> **Architecture Shift**: We are moving from a dynamic Axum server for the starter-pack to a **Static JSON Generator** (`generate_payload.rs`). This ensures zero cold-start latency and zero hosting costs for the metadata.

## Proposed Changes

### 1. Android Domain Model (core:model)
#### [MODIFY] [CosmeticItem.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/model/src/main/java/com/zoewave/probase/core/model/ritual/CosmeticItem.kt)
- **New Enum**: Add `Temperature` enum (WARM, COOL, NEUTRAL, OLIVE, UNKNOWN).
- **Field Update**: Add `val temperature: Temperature` to the `CosmeticItem` data class.

### 2. Room Persistence (applications:kocolor:db)
#### [MODIFY] [CosmeticItemEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/CosmeticItemEntity.kt)
- **Field Update**: Add `val temperature: Temperature` to match the domain model.

### 3. Rust Payload Generator
#### [MODIFY] [generate_payload.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/kocolor/src/bin/generate_payload.rs)
Update the existing generator to be fully compliant with the **Glow Archive Taxonomy**.
- **Structural Alignment**: Implement the `StarterPackResponse` wrapper and the full `CosmeticItem` struct (including Level 1, 2, and 3 facets).
- **Data Integrity**: Seed the "Signature Crimson Lip Color" with its correct facets from `ProfessionalTaxonomy.md`.
- **UI Visuals**: Add the `color_hex` field (e.g., `#5A1827` for the Crimson Lip Color).
- **URL Correction**: Ensure `image_url` points to the clean CDN path.

### 4. Infrastructure Documentation
#### [NEW] [DeployArch.md](file:///Users/developer/AndroidStudioProjects/ProBase/server/docs/Inventory/Handbook/DeployArch.md)
Document the hybrid strategy: GitHub Pages for static data/assets and Hugging Face for dynamic AI orchestration.

## Verification Plan

### Automated Tests
- Run `cargo run --bin generate_payload` and verify the `starter-pack.json` output.
- Compile Android modules `core:model` and `applications:kocolor:db` to verify type-safety.

### Manual Verification
- Verify the generated URLs point to the correct `cdn.kocolor.com` path.
- Check the JSON for the "Signature Crimson Lip Color" reference item to ensure `temperature: "Neutral"` is present.
