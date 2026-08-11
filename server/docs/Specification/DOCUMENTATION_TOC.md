# Master Documentation Index: KoColor Sovereign Distribution Platform

This index provides a centralized Table of Contents for all architectural blueprints, data contracts, and operational guides within the KoColor ecosystem.

---

## 🏗️ 1. Core Platform Manifestos
*These documents define the "What" and "Why" of the entire system architecture.*

*   **[Anchor.md](./Anchor.md)**: The foundational "Data as Code" manifesto and Hub-and-Spoke model.
*   **[Evolution.md](./Evolution.md)**: The historical record of our taxonomic and architectural transitions.
*   **[Walkthrough.md](./Walkthrough.md)**: The comprehensive end-to-end journey of a product through the platform.
*   **[implementation.md](./implementation.md)**: The authoritative technical summary of the V1 infrastructure.

---

## ⚙️ 2. Server & Compiler Infrastructure
*Technical specifications for the Rust-based distribution and security pipeline.*

### Data Contracts (Schemas)
*   **[KCPS v1 SPEC](./Schema/KCPS_v1_SPEC.md)**: The strict wire-format contract for distribution packages (`.kpkg`).
*   **[KPSS v1 SPEC](./Schema/KPSS_v1_SPEC.md)**: The minimalist authoring contract for raw product data.
*   **[Product Authoring Guide](./Schema/KCPS_Product_Authoring_Guide.md)**: Manual for authors creating KPSS JSON and raw assets.

### Distribution Security
*   **[Streaming Integrity Architecture](./Pipeline/Streaming_Integrity_Architecture.md)**: Hash-in-filename and secure spooling logic.
*   **[JSON Bomb Prevention](./Pipeline/JSON_Bomb_Prevention.md)**: Countermeasures against memory-exhaustion attacks.

### Asset Engineering (Phase 4 & 5)
*   **[Assets Engineering Pipeline](./ProductGen/AssetsEngineeringPipeline.md)**: The CCT (Compute-at-Compile-Time) visual and math specs.
*   **[User Manual](./ProductGen/USER_MANUAL.md)**: Operational guide for the `kc-optimizer` toolchain.
*   **[Team Workflow](./ProductGen/Team_Workflow.md)**: The step-by-step "Drop JSON & Run" guide for the team.

---

## 📱 3. KoColor Mobile App (Android Hub)
*Implementation details for the primary verification and ingestion gateway.*

*   **[Mobile Ingestion Implementation](../../applications/kocolor/docs/Pipeline/Mobile_Ingestion_Implementation.md)**: Blueprint for verified streaming, Zstd, and Room sync.
*   **[Ingestion & Security Walkthrough](../../applications/kocolor/docs/Walkthrough_Mobile_Ingestion_Enrichment.md)**: Technical guide for scientific enrichment, BlurHash, and "Make it Mine" cloning.
*   **[Boutique UI Specification](../../applications/kocolor/docs/UI/Implementation_V1_Core.md)**: High-fidelity selection UI and sticky header constraints.
*   **Feature Modules**:
    *   [BoxCapture](../../applications/kocolor/docs/BoxCapture/walkthrough.md)
    *   [GenAI Engine](../../applications/kocolor/docs/GenAI/walkthrough.md)
    *   [Wardrobe Engine](../../applications/kocolor/docs/WardrobeColorEngine/WardrobeColorEngine.md)

---

## 📁 4. Historical Archive
*Reference-only documentation for old walkthroughs and outdated proposals.*

*   **[Archive Directory](./Archive/)**: Contains all legacy security phases, old UI/UX prototypes, and historical project reviews.

---
**Status**: 🚀 **V1 PLATFORM READY**
**Last Indexed**: 2026-08-10
