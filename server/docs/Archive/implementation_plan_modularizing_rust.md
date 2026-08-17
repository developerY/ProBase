# Implementation Plan: Modularizing KoColor Rust Backend & Data

This plan outlines the refactoring of the `kocolor` Rust project into a more professional, modular structure. We will move the domain models and the hardcoded "Starter Pack" data into separate files to keep the generator script clean and maintainable.

## User Review Required

> [!NOTE]
> **Project Structure**: I am introducing a library component (`src/lib.rs`) to the `kocolor` package. This allows sharing the `CosmeticItem` and `ClothingItem` structs between the generator CLI and the Axum server.

## Proposed Changes

### 1. Package Configuration
#### [MODIFY] [Cargo.toml](file:///Users/developer/AndroidStudioProjects/ProBase/server/KoColor/Cargo.toml)
Explicitly define the library and the binary target.

### 2. Library Core
#### [NEW] [src/lib.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/KoColor/src/lib.rs)
Move all Serde-serializable structs (`CosmeticItem`, `ClothingItem`, `StarterPackResponse`) here.
#### [NEW] [src/inventory.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/KoColor/src/inventory.rs)
Create a dedicated module for the "Manufacturer Data." This will contain functions to return the curated list of items like the "Glow Catalyst Lip Stain."

### 3. Generator Refinement
#### [MODIFY] [src/bin/generate_payload.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/KoColor/src/bin/generate_payload.rs)
- Remove hardcoded item definitions.
- Import structs and data from the parent crate (`kocolor::*`).
- Focus solely on the file I/O and serialization logic.

## Verification Plan

### Automated Tests
- Run `cargo check` to ensure the library/binary linkage is correct.
- Run `cargo run --bin generate_payload` and verify the `starter-pack.json` is still generated correctly.

### Manual Verification
- Inspect `src/inventory.rs` to ensure the "Glow Catalyst Lip Stain" data is preserved with max-fidelity.
