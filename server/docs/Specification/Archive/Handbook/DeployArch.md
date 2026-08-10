# Deployment Architecture: KoColor Distributed Logic

To ensure 100% uptime, zero cold-start latency, and zero hosting costs for the KoColor "Glow Archive," we utilize a distributed, hybrid architecture.

## 1. Static Metadata CDN (GitHub Pages)
The primary source of truth for the product taxonomy and starter data is a static JSON payload hosted at `cdn.kocolor.com`.

*   **Host:** GitHub Pages.
*   **Artifact:** `starter-pack.json`.
*   **Payload Versioning:** The JSON includes a `version` field. The Android client caches this version locally and only re-downloads if the remote version increases.
*   **Image Assets:** All images are hosted as optimized `.webp` files in the same CDN environment.

## 2. Dynamic Intelligence Node (Hugging Face)
While metadata is static, dynamic AI workflows (such as advanced skin analysis or real-time layering simulations) are orchestrated via Hugging Face Docker Spaces.

*   **Host:** Hugging Face Spaces.
*   **Runtime:** Rust (Axum).
*   **Responsibility:** Interfacing with LLMs (Gemini), executing heavy computational chemistry simulations, and providing a proxy for secure API key injection.
*   **Sleep Cycle:** Because this node is dynamic, it may "sleep" on the free tier. The Android app only wakes it when the user initiates an AI-driven ritual, ensuring the base app remains functional even if the AI node is booting.

## 3. Local SSOT (Android Room)
Regardless of the origin, all data is persisted in a local-first Room database.

*   **Origin-Blindness:** The app doesn't care if a `CosmeticItem` came from the `starter-pack.json` or a local camera scan—they are stored in the same `@Entity` structure.
*   **Local URIs:** All image data is resolved to `file://` URIs pointing to the app's internal storage, ensuring the vanity remains accessible without an internet connection.

---
**Status**: Architecture Finalized
**Usage**: Guide for backend maintenance and scaling.
