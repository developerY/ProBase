# Taxonomy Alignment Tasks

- `[x]` Android Domain Model Alignment
    - `[x]` Add `Temperature` enum to `CosmeticItem.kt`
    - `[x]` Update `CosmeticItem` with `temperature` field
- `[x]` Persistence Layer Alignment
    - `[x]` Update `CosmeticItemEntity` with `temperature` field
    - `[x]` Implement `Temperature` converters in `FashionConverters.kt`
- `[x]` Distributed Backend Infrastructure
    - `[x]` Create `StarterPackDtos.kt` for data ingestion
    - `[x]` Implement `KocolorApiService` for remote data fetching
    - `[x]` Update `CosmeticInventoryRepository` with `ingestStarterPack()`
- `[x]` Rust Tooling
    - `[x]` Refine `generate_payload.rs` for taxonomy parity
