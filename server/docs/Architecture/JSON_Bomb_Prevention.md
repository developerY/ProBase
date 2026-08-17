# Technical Specification: JSON Bombing Defense & Memory Safety

This document details the architectural countermeasures implemented in the KoColor distribution platform to prevent "JSON Bombing" and memory-exhaustion Denial-of-Service (DoS) attacks during package ingestion.

---

## 🏗️ 1. The Threat: What is a JSON Bomb?

A JSON (or Zip) Bomb is a small, maliciously crafted payload (e.g., 10 KB) that contains recursively nested structures or massive arrays. When a standard parser attempts to decompress or deserialize this data, it expands exponentially (e.g., to 5 GB), causing the mobile device to run out of RAM, crash, or freeze.

---

## 🛡️ 2. The Defensive Layers

The KoColor pipeline enforces a "Fortress Ingestion" model with four distinct layers of protection.

### Layer A: The Signed Size Contract
In standard ingestion, the app "discovers" the size during decompression. In KoColor, the **`uncompressed_size_bytes`** is part of the **Ed25519-signed manifest**.

*   **Logic**: The app knows exactly how many bytes will be produced before it even touches the Zstd engine.
*   **Security**: Because the manifest is signed, an attacker cannot forge a large file size without breaking the cryptographic signature.
*   **Rejection**: The app checks the signed size against a hardcoded **32 MB safety limit**. Any manifest entry exceeding this is rejected before a single byte is downloaded.

### Layer B: Zero-Heap Spooling (Disk First)
Compressed binary bytes arriving from the network are **never** loaded into the application's RAM as an active object.

*   **Logic**: Using `HashingSink` and `okio`, bytes flow directly from the network socket to a temporary file on the internal disk.
*   **Security**: If an attacker sends a 100 MB "Network Bomb," the app catches the size mismatch against the manifest metadata and **terminates the connection** immediately. Your app heap remains clean.

### Layer C: Pre-allocated Decompression (Fixed Bounds)
When the app finally decompresses the verified `.kpkg` file, it does not use a dynamic "stream-to-object" approach.

*   **Logic**: It uses the verified `uncompressed_size_bytes` from the signed manifest to allocate a **fixed-size buffer**.
*   **Security**: The Zstd engine is mathematically restricted to that specific byte-count. It cannot "run away" and consume extra memory because the buffer size is immutable and bound by the metadata **you** signed at compile-time.

### Layer D: Content-Addressed Integrity (SHA-256)
The filename and manifest both store a SHA-256 hash of the *compressed* binary.

*   **Security**: An attacker cannot "re-sign" a bomb. If they modify the binary to be a bomb, the SHA-256 hash in the filename will break. If they modify the manifest to match the bomb's hash, your private-key Ed25519 signature will fail verification.

---

## 📊 3. Comparison of Safety Models

| Feature | Standard App (Unsafe) | KoColor Hub (Sovereign) |
| :--- | :--- | :--- |
| **Trust Model** | Trust-on-Arrival | **Verify-Before-Execute** |
| **Memory Allocation** | Dynamic (Grows with data) | **Pre-allocated (Bound by signature)** |
| **Integrity Check** | Post-Parse (Too late) | **Pre-Decompression (Incremental)** |
| **Size Knowledge** | Reactive (Discovered) | **Predictive (Signed Contract)** |

---

## ✅ Conclusion

By combining **Cryptographic Signing**, **Content-Addressing**, and **Zero-Heap Spooling**, the KoColor architecture ensures that malicious or corrupt data can never trigger a parser exploit or exhaust device resources.

**Status**: 🛡️ **BOMB-PROOF INGESTION ACTIVE**
**Safety Cap**: 32.0 MB (Hardcoded)
**Verification**: Native Ed25519 + SHA-256
