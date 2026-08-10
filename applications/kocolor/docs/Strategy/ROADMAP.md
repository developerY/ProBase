# Roadmap: Inventory Provenance & Pack Management

This document outlines the long-term architectural vision for the KoColor inventory system, transitioning from simple data ingestion to a robust, scalable platform.

---

## 🚨 1. Foundational Requirements (Must-Have)

These are critical for production stability and user trust.

*   **Transactional Wiping**: selective pack deletion must be a single Room `@Transaction`. If deleting a pack fails mid-way, the database must roll back to avoid "orphaned" products.
*   **Clone Detachment**: When a user selects "Make it Mine" (cloning a pack template), the app must:
    *   Set `InventorySource` to `CLONED`.
    *   Clear `sourcePackId` to protect it from batch wipes.
    *   Store the original ID in `parentItemId` for lineage tracking.
*   **Idempotent Ingestion**: Use `OnConflictStrategy.REPLACE` to ensure multiple sync attempts of the same pack version don't duplicate items.
*   **Offline Resilience**: The Sync Hub must render the last cached `manifest.json` from Room when no connection is available, allowing management of installed packs while offline.

---

## 🛡️ 2. Future-Proofing (Zero-Cost Schema Additions)

We have added these fields today to avoid costly database migrations later:

### `InstalledPackEntity`
*   `hash`: Verify payload integrity.
*   `installedAt`: Sort packs by recency.
*   `sizeBytes`: Show user the download weight (e.g., "1.2 MB").
*   `expiresAt`: Support time-limited promotional packs.
*   `heroImageUrl`: Visual branding for curated kits.

### `CosmeticItem` & `ClothingItem`
*   `isHidden`: Soft-delete or filter items from the AI engine without wiping them.
*   `parentItemId`: Track the "genetic lineage" of cloned items for analytics.
*   `sourceType`: New enums `CLONED` and `COMMUNITY` for peer sharing.

---

## 🚀 3. Long-Term Vision (The Platform Engine)

High-impact features that transform the app into a business.

*   **AI Context Management**: As the database grows to 500+ items, we will implement a "Pre-filtering" layer to only feed the most relevant 20 items to Gemini's context window, preventing latency and token bloat.
*   **Pack Deep Linking**: Direct-to-pack navigation from social media or email (e.g., kocolor://sync/spring-2026).
*   **Peer-to-Peer Sharing**: "Export as Pack" functionality allowing users to share their custom wardrobes with friends via JSON payloads.
*   **Commerce Funnels**: "Buy Full Size" buttons on partner pack items, routing users to affiliate checkouts with one tap.
*   **Delta Updates**: Implement JSON patching/diffs to update single item facets (like price or hex) without re-downloading the entire collection.
*   **Cloud Vault**: Optional Google Drive/Cloud backup for custom item metadata and the `InstalledPack` registry for seamless device transitions.

---
**Status**: ✅ Foundations Coded | ✅ Schema Future-Proofed
**Next Phase**: Section 3 (Implementation Ready)
