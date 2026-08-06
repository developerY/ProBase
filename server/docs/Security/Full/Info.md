Here is the complete architectural documentation detailing the implementation of advanced compression and partner IP protection.

You can save this directly into your repository's `docs/` folder as `ADVANCED_PACKAGING_ARCHITECTURE.md`.

---

# Architecture Extension: Advanced Compression & IP Protection

This document outlines the strategic evolution of the KoColor Secure Package Distribution Platform. It details the transition from raw JSON payloads to a highly compressed, optionally encrypted binary format (`.kpkg`) designed to minimize CDN bandwidth and protect proprietary partner IP (e.g., formulations, AI styling tags) from competitor scraping.

## 1. The Split Architecture (Public vs. Protected)

To maintain a fast, fluid user experience in the Android Glow Sync Hub without decrypting massive payloads on the fly, the architecture splits data into two distinct layers: **Public Metadata** and **Protected Content**.

### The Public Manifest (`manifest.json`)

The manifest remains in **plaintext JSON**. It acts as the public catalog and root of trust, containing enough metadata to render UI previews and power local search without touching the actual package data.

```json
{
  "packs": [
    {
      "id": "com.kocolor.pack.mac.core",
      "version": "1.1.0",
      "publisher": "MAC Cosmetics",
      "category": "lips",
      "thumbnail_url": "https://cdn.kocolor.com/assets/mac-core-thumb.webp",
      "search_tags": ["matte", "ruby woo", "red"], 
      "sha256": "a1b2c3d4...", 
      "signature": "e5f6g7h8..."
    }
  ]
}

```

### The Protected Package (`.kpkg`)

The individual package is no longer a raw JSON file. It is a proprietary KoColor Package (`.kpkg`) binary. It contains the heavy, sensitive payload (full ingredients, extended attributes, AI nodes) and is completely unreadable to casual network scanners.

## 2. The `.kpkg` Pipeline Strategy

The system utilizes a sequential pipeline in the Rust Normalization Compiler to transform canonical data into a `.kpkg` file.

### Step 1: Canonicalization (The Foundation)

External vendor data is mapped into the strictly typed `CanonicalPackItem` Rust structs.

### Step 2: Zstandard (zstd) Compression

Instead of relying on HTTP transit compression (Gzip), the Rust compiler applies **Zstandard (zstd)** directly to the byte array.

* **Why Zstd?** It offers 4–6x compression ratios at significantly higher decompression speeds on mobile devices compared to standard Gzip.
* **Result:** A 5MB JSON cosmetics database becomes a ~800KB binary blob.

### Step 3: AES-256-GCM Encryption (Optional per Partner)

If a partner requires IP protection, the compressed bytes are encrypted using AES-256-GCM.

* **Why GCM?** Galois/Counter Mode provides both data confidentiality and authenticated encryption.

### Step 4: Ed25519 Signing (The Anchor)

The final compressed (and optionally encrypted) byte array is hashed (SHA-256) and signed using the KoColor Ed25519 Private Key. This guarantees the package was not tampered with after compilation.

---

## 3. The End-to-End Flow Diagram

```mermaid
graph TD
    subgraph Rust Compiler (Backend)
        A[Vendor JSON] --> B[Canonical DTO Mapping]
        B --> C[Serialize to Bytes]
        C --> D[Zstandard Compression]
        D --> E{Encrypt?}
        E -- Yes --> F[AES-256-GCM]
        E -- No --> G
        F --> G[SHA-256 Hash]
        G --> H[Ed25519 Signature]
        H --> I[Output: .kpkg Binary]
    end

    subgraph CDN (Cloudflare)
        I --> J((Cloudflare Edge))
        M[manifest.json] --> J
    end

    subgraph Android Client (Zero-Trust Receiver)
        J --> K[Download .kpkg]
        K --> L[Verify Ed25519 Signature]
        L -- Pass --> N{Is Encrypted?}
        N -- Yes --> O[Fetch AES Key via KMS]
        O --> P[Decrypt AES-256]
        N -- No --> Q
        P --> Q[Decompress Zstd]
        Q --> R[Deserialize to Kotlin DTO]
        R --> S[(Room DB @Transaction)]
        L -- Fail --> T[Throw PackException]
    end

```

---

## 4. The Encryption Reality: Key Management Service (KMS)

**The Threat Model:**
If a package is encrypted on the CDN, the Android app requires the symmetric AES decryption key to open it. **Hardcoding this AES key inside the Android APK is a critical security vulnerability.** A malicious actor can decompile the APK, extract the hardcoded key, and decrypt the CDN files.

**The KMS Solution:**
To achieve true IP protection, KoColor will implement a lightweight Key Management Service (KMS) separated from the static CDN.

1. **Discovery:** The Android app downloads the static `.kpkg` from the free CDN.
2. **Authentication:** When the user clicks "Import", the app makes an authenticated, lightweight API request to a KoColor server (e.g., Firebase Auth / Cloud Run): *"User 123 is requesting the AES key for pack `kc-001`."*
3. **Delivery:** The server verifies the user's authorization and returns the specific AES key for that pack.
4. **Decryption:** The app decrypts the `.kpkg` purely in memory and persists the cleartext data into the local Room Database. The AES key is discarded.

*Note: This keeps 99% of your bandwidth (the heavy packages) on the free CDN, while only routing a few kilobytes of traffic (the AES keys) through your dynamic servers.*

---

## 5. Strategic Implementation Roadmap

This architecture will be implemented in two distinct phases to ensure stability.

### Phase 1: The Compression Era (Bandwidth Optimization)

* **Goal:** Reduce CDN costs and speed up Android import times.
* **Action (Rust):** Add the `zstd` crate. Update the compiler to compress the payload before signing. Change the output extension to `.kpkg`.
* **Action (Android):** Add a Zstd JNI library. Update the `SignatureVerifier` to decompress the byte array immediately after validating the Ed25519 signature.

### Phase 2: The Privacy Era (IP Protection)

* **Goal:** Protect sensitive partner data from unauthorized scraping.
* **Action (Cloud):** Stand up a lightweight KMS endpoint to serve symmetric AES keys to authenticated app users.
* **Action (Rust):** Add the `aes-gcm` crate. Encrypt the compressed payload before signing.
* **Action (Android):** Add decryption logic between the Signature Verification and Decompression steps, utilizing the securely fetched KMS keys.