**Role & Context:**
You are an Expert Rust Backend Architect. We are building a local data-seeding backend for an offline-first Android application called KoColor. The Android app relies on a local SQLite (Room) database and requires an initial "Starter Pack" of data to solve the zero-item problem on first launch.

**Task:**
Generate a Rust web server using the `axum` framework. The server must be structured as a Cargo Workspace within a monorepo.

**1. Directory & Workspace Structure:**
*   Create a root master `Cargo.toml` at `server/` that defines a workspace containing one member: `"kocolor"`.
*   Create the actual Axum server package inside `server/kocolor/`.
*   Include necessary dependencies in `server/kocolor/Cargo.toml`: `axum`, `tokio` (with full features), `serde` (derive), `serde_json`, and `base64`.

**2. Domain Models (The Taxonomy):**
In `server/kocolor/src/main.rs`, define strict Serde-serializable structs for the JSON response. The taxonomy must perfectly match this structure:
*   `StarterPackResponse` containing `cosmetics: Vec<CosmeticItem>` and `clothing: Vec<ClothingItem>`.
*   `CosmeticItem` and `ClothingItem` must contain the following String fields:
    *   `id` (Primary Key)
    *   `macro_category` (Level 1)
    *   `micro_category` (Level 2)
    *   `name`
    *   `formulation` (Level 3 Facet)
    *   `chemistry` (Level 3 Facet)
    *   `finish` (Level 3 Facet)
    *   `coverage` (Level 3 Facet)
    *   `temperature` (Level 3 Facet)
    *   `color_hex` (UI Visual)
    *   `image_data` (Must be formatted as a standard Data URI string containing Base64 encoded bytes).

**3. Endpoint Implementation:**
*   Create a single `GET /api/v1/starter-pack` endpoint.
*   The handler must read physical image files from a local `server/kocolor/assets/` directory, encode them into Base64 using standard encoding, and construct the `data:image/jpeg;base64,...` URI string for the `image_data` field.
*   **Mock Data Requirement:** Instantiate at least one `CosmeticItem` in the response payload (e.g., "KoColor Signature Crimson Lip Color"). For this specific item, you must read and encode the local image file named verbatim as `image_07157d.jpg`.
*   Return the assembled `StarterPackResponse` as an Axum `Json` response.

**4. Execution:**
*   Provide the code for `server/Cargo.toml`.
*   Provide the code for `server/kocolor/Cargo.toml`.
*   Provide the complete, fully functioning Rust code for `server/kocolor/src/main.rs`.
*   Ensure the Axum server binds to `0.0.0.0:3000` via Tokio.