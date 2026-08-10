# Technical Specification: Streaming Integrity & Content-Addressed Security

This document explains the "Secure Spooling" architecture of the KoColor distribution platform. It details how the hash-in-filename pattern ensures safety during binary streaming and prevents malicious payload execution.

---

## 🏗️ 1. The Multi-Layer Trust Framework

Security is enforced at three distinct stages before a single byte of product data is written to the user's database.

### Stage A: The Root of Trust (Signed Manifest)
Before the app downloads a `.kpkg` file, it pings the `manifest.json`. 
1.  **Authenticity**: The manifest is cryptographically signed by the developer's **Ed25519** private key.
2.  **Verification**: The Android app verifies this signature using the public key embedded in the APK.
3.  **The Contract**: Once verified, the app now has a "Trusted Registry" of every available package ID and its **Expected SHA-256 Hash**.

### Stage B: Content-Addressed Streaming
The package filename follows the pattern: `com.kocolor.pack.{id}-{sha256}.kpkg`.

1.  **Immutability**: The hash in the filename is a direct mathematical representation of the compressed bytes. If a single bit is changed (e.g., a "man-in-the-middle" attack or CDN corruption), the hash becomes invalid.
2.  **Discovery**: The app uses the `endpoint` URL from the verified manifest to start the stream.

### Stage C: Secure Spooling (Incremental Integrity)
This is where the "Safety" happens during the actual download.

```kotlin
// From StarterPackRepository.kt
val hashingSink = HashingSink.sha256(tempFile.sink())
responseBody.source().use { source ->
    hashingSink.buffer().use { sink ->
        sink.writeAll(source) // Spooling and hashing simultaneously
    }
}
```

1.  **Zero-Heap Spooling**: As bytes arrive from the network, they are written directly to a temporary file on disk. They are **never** loaded into memory as an active object during this phase.
2.  **Incremental Hashing**: The `HashingSink` calculates the SHA-256 hash *while* the bytes are being written.
3.  **The "Kill Switch" Check**: Immediately after the stream ends, the app compares `hashingSink.hash` against the **Expected Hash** from the signed manifest.
4.  **Early Rejection**: If the hashes do not match, the temporary file is deleted instantly. The app **never** attempts to decompress or parse the data.

---

## 🛡️ 2. Why the Filename Hash is Critical

Including the hash in the filename provides three architectural safeguards:

| Threat | Safeguard |
| :--- | :--- |
| **CDN Poisoning** | If a hacker replaces the file on the server with a malicious one, the hash won't match. The app detects this instantly and deletes the file. |
| **Version Drift** | If you deploy a new version of the "Core Collection" but a user's phone still has an old manifest, the app won't find the old filename. This prevents "Partial Sync" bugs. |
| **Cache Corruption** | If a partial download is cached by the CDN or a local proxy, the SHA-256 check fails during spooling, preventing the ingestion of broken/truncated JSON. |

---

## 🏁 3. Summary: The "Verify-Before-Execute" Rule

In the KoColor ecosystem, we enforce a strict **Verify-First** policy:
1.  **Spool** (to disk)
2.  **Verify Integrity** (SHA-256 vs. Signed Manifest)
3.  **Verify Authenticity** (Ed25519 Signature)
4.  **Decompress & Parse** (Only after all trust checks pass)

**This architecture ensures that malicious or corrupt data can never trigger a parser exploit or bloat the mobile heap.**
