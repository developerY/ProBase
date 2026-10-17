# Implementation Plan - Starter Pack Upgrade & Secure Architecture

Upgrade the existing Starter Pack system in KoColor to support global search, selective item ingestion, and a cryptographically secure, versioned data architecture.

## User Review Required

> [!IMPORTANT]
> The search index and pack data are hosted on a CDN (e.g., Cloudflare).
> **New Requirement**: All payloads (Manifests and Packs) are now wrapped in a `SignedPayloadEnvelope` and must pass signature verification before ingestion.

## Technical Refinements

- **Search Debouncing**: The `StarterPackViewModel` applies a `debounce(300L)` to the `searchQuery` StateFlow.
- **Scroll-to-Item Logic**: `PackPreviewScreen` maps `targetItemId` to an index and uses `animateScrollToItem(index)`.
- **Cryptographic Verification**: A `SignatureVerifier` service validates incoming payloads against a public key.
- **Data Provenance**: Every imported item is tagged with a `Provenance` object containing its source pack, version, and verification status.

## Proposed Changes

### [Data Layer - Room Database]

#### [NEW] [Provenance.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/Provenance.kt)
- Define `Provenance` data class for Room: `packId`, `packVersion`, `publisher`, `installedAtTimestamp`, `isSignatureVerified`.

#### [MODIFY] [CosmeticItemEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/CosmeticItemEntity.kt)
- Replace `sourcePackId: String?` with `@Embedded val provenance: Provenance?`.

#### [MODIFY] [CosmeticDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/dao/CosmeticDao.kt)
- Update `deleteCosmeticsByPackId` query to filter by `provenance_packId`.

---

### [Data Layer - Network & DTOs]

#### [MODIFY] [SignedPayloadEnvelope.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/model/SignedPayloadEnvelope.kt)
- Refactor to `SignedPayloadEnvelope<T>` with `data`, `signature`, and `version`.

#### [MODIFY] [KocolorApiService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/KocolorApiService.kt)
- Update endpoints to return `SignedPayloadEnvelope<T>`.

#### [MODIFY] [PackItem.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/model/PackItem.kt)
- Add canonical schema fields: `formulation`, `finish`, `coverage`, `temperature`, `macroCategory`, `microCategory`.

---

### [Repository Layer - Verification Pipeline]

#### [MODIFY] [StarterPackRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt)
- Integrate `SignatureVerifier`.
- Implement `Provenance` caching during the fetch-to-import lifecycle.
- Throw `PayloadVerificationException` on signature mismatch.

---

### [ViewModel Layer]

#### [MODIFY] [StarterPackViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/StarterPackViewModel.kt)
- Manage `SyncHub` state with debounced search.

#### [NEW] [PackPreviewViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/PackPreviewViewModel.kt)
- Manage multi-selection and selective ingestion.

---

### [UI Layer]

#### [MODIFY] [SyncHubScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/SyncHubScreen.kt)
- Implement `DockedSearchBar` and hub navigation.

#### [NEW] [PackPreviewScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/ui/PackPreviewScreen.kt)
- Implement picker UI with auto-scroll and row highlighting.

## Verification Plan

### Automated Tests
- Unit tests for `SignatureVerifier` logic.
- Unit tests for `Provenance` mapping in `CosmeticMapper`.
- Unit tests for ViewModel filtering and selection.

### Manual Verification
- Verify that tampered JSON files (signature mismatch) trigger a verification error.
- Confirm imported items in Room have complete `Provenance` metadata.
- Verify search-to-item highlighting works correctly.
