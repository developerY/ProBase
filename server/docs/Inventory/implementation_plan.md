# Implementation Plan: KoColor Rust Starter-Pack Backend

This plan outlines the steps to transform the existing Rust server into a structured Cargo Workspace that provides a local data-seeding API for the KoColor Android application.

## User Review Required

> [!IMPORTANT]
> **Asset Dependency**: The implementation assumes that image files (e.g., `image_07157d.jpg`) are placed in the `server/KoColor/assets/` directory. I will create this directory and add a placeholder note if the files are not yet present.

> [!NOTE]
> **Port Change**: The current server runs on `8080`. Per the `prompt.md` requirement, I will move it to `3000` to align with the development specification.

## Proposed Changes

### 1. Workspace Configuration
#### [NEW] [Cargo.toml](file:///Users/developer/AndroidStudioProjects/ProBase/server/Cargo.toml)
Create a root workspace file to manage the backend components.
```toml
[workspace]
members = ["KoColor"]
resolver = "2"
```

### 2. Package Configuration
#### [MODIFY] [Cargo.toml](file:///Users/developer/AndroidStudioProjects/ProBase/server/KoColor/Cargo.toml)
Update the package name and add the `base64` crate for image processing.
```toml
[package]
name = "kocolor"
# ... dependencies: add base64
```

### 3. Server Logic
#### [MODIFY] [main.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/KoColor/src/main.rs)
- **Domain Models**: Implement Serde structs for `StarterPackResponse`, `CosmeticItem`, and `ClothingItem` following the three-tier taxonomy.
- **Image Helper**: Add `encode_image_to_base64` to convert local assets into `data:image/jpeg;base64` strings.
- **Handler**: Implement `get_starter_pack` which seeds the initial "Classic" items (Crimson Lip Color, Velvet Primer, etc.).
- **Routing**: Bind `GET /api/v1/starter-pack` to the handler.

### 4. Asset Management
#### [NEW] [assets/](file:///Users/developer/AndroidStudioProjects/ProBase/server/KoColor/assets/)
Ensure the directory exists for storing the reference product images.

## Verification Plan

### Automated Tests
- I will attempt a `cargo build` within the sub-agent to verify syntax correctness.
- Once deployed, the endpoint can be verified via:
  `curl http://localhost:3000/api/v1/starter-pack`

### Manual Verification
- Verify the JSON response contains the correct Base64 strings.
- Check that the `chemistry`, `formulation`, and `temperature` facets match the `ProfessionalTaxonomy.md` specs.
