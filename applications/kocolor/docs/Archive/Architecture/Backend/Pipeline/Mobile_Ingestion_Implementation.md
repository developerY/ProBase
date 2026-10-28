# Implementation Plan: Mobile Ingestion Phase (Android Hub)

This document outlines the technical execution path for building the secure "receiver" for KoColor distribution artifacts. We are implementing the client-side counterpart to the Sovereign Distribution Platform, focusing on cryptographic verification, binary decompression, and atomic persistence.

---

## 🏗️ 1. Cryptographic Sync Engine

The mobile app must establish a "Root of Trust" before any data is ingested into the user's archive.

### Tasks:
*   **Signature Verification**: Implement native Android security logic (`java.security.Signature`) to verify the **Ed25519** developer signature on the `manifest.json`.
*   **Hash Anchoring**: Extract the **SHA-256** hashes from the verified manifest. These act as the immutable "Anchors" for the subsequent binary downloads.
*   **Identity Bootstrap**: Store the Developer Public Key securely within the app binary to prevent "Rogue Manifest" attacks.

---

## 📦 2. Verified Binary Streaming (Zstd)

Ingestion must be efficient and secure, protecting the device from memory-exhaustion (JSON Bombing).

### Tasks:
*   **Secure Spooling**: Use `okio` and `HashingSink` to stream `.kpkg` binaries directly to internal disk. Calculate the SHA-256 hash incrementally during the download.
*   **The "Kill Switch"**: Immediately delete the temporary file if the calculated hash does not match the anchor hash from the signed manifest.
*   **Bounded Decompression**: Integrate the **Zstd-JNI** library. Use the signed `uncompressed_size_bytes` to allocate a fixed-size buffer, preventing runaway memory consumption during decompression.

---

## 💾 3. Relational Mapping & Atomic Persistence

The app must transform the "Wire Object" (**KCPS v1**) into long-term **Room** entities.

### Tasks:
*   **Polymorphic Parsing**: Use `kotlinx.serialization` to deserialize the decompressed JSON into the `KcpsPayload` sealed hierarchy.
*   **Entity Mapping**: Map the enriched metadata (CIELAB, Safety Flags, BlurHash) into `CosmeticItemEntity` and `ClothingItemEntity` schemas.
*   **Transactional Sync**: Wrap the entire ingestion (Insert items + Record package installation) in a single **Database Transaction** to ensure the inventory state is never partially synchronized.

---

## 🎨 4. High-Fidelity Boutique UI

The UI must utilize the pre-calculated build-time intelligence for a premium user experience.

### Tasks:
*   **BlurHash Placeholders**: Implement a custom `Painter` or Coil `Transformation` that instantly renders the `calculated_blurhash` as a blurred background while the high-res WebP image loads.
*   **Zero-Latency Filters**: Wire the "Boutique Counter" filters to query the pre-calculated `safety_flags` and `search_tokens` in Room, ensuring instantaneous UI updates.
*   **Personalization Engine**: Implement the **"Make it Mine"** cloning logic, ensuring personalized items are detached from their source collection and protected during a collection wipe.

---

## 🛠️ Implementation Checklist

- [x] **Task 1: Security Integration**
    - Integrated **Tink** for Ed25519 verification and Public Key anchoring.
- [x] **Task 2: Zstd Compression Engine**
    - Setup `zstd-jni` dependency and implemented **bounded decompression** with safety caps.
- [x] **Task 3: Streaming Ingestion Pipeline**
    - Built the `HashingSink` downloader with **Verify-Before-Execute** early-rejection logic.
- [x] **Task 4: Room Persistence & Mapping**
    - Created the transactional seeding logic and implemented the **"Make it Mine"** cloning mechanism.
- [x] **Task 5: Boutique UI Construction**
    - Built the Jetpack Compose selector with **BlurHash** instant placeholder support.

---
**Status**: 🗓️ **PLANNING COMPLETE**
**Platform**: Android 14+ (SDK 34)
**Lead**: Android Mobile Architect
