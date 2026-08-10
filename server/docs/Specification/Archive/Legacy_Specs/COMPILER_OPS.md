# KoColor Compiler Operations

> [!NOTE]
> This document provides detailed operational procedures. For a high-level architectural overview, see the main [server/README.md](../../README.md).

## 1. Cryptographic Key Management

The KoColor platform relies on an **Ed25519 Keypair**. The Private Key signs the packages, and the Public Key verifies them on the Android device.

### Generating the Keypair
The safely way to generate a keypair is using the included Rust utility:

```bash
cd server/gen/Key
cargo run --bin keygen
```

### Root of Trust (Public Key)
The Public Key is safe to distribute. It MUST be compiled directly into the Android application to prevent man-in-the-middle attacks.

*   **Location**: `applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/SecurityConstants.kt`

---

## 2. Environment Configuration

The compiler requires the private key to sign the `.kpkg` binaries. 

1. Create a `.env` file in the root of the Rust compiler project (`server/package/KoColor/`).
2. Add the private key:
```env
CDN_PRIVATE_KEY_HEX=your_64_character_hex_private_key
```
3. Ensure `.env` is listed in your `.gitignore`.

---

## 3. The Transformation Pipeline

The Rust compiler executes the following sequence to ensure a valid and secure package:

1.  **Serialization**: minified JSON byte stream.
2.  **Compression**: Zstandard (Zstd) Level 3.
3.  **Hashing**: SHA-256 of the compressed bytes.
4.  **Signing**: Ed25519 signature of the compressed bytes.
5.  **Artifact Generation**: Writes `<id>-<sha256>.kpkg`.

---

## 4. Troubleshooting Manifest Mismatches

If the Android app reports a `SignatureException` during the "Trust Bootstrap" phase:

1.  **Check Whitespace**: Ensure your Rust compiler isn't adding trailing newlines to the `data` object in `manifest.json`.
2.  **Check Key ID**: Verify that the `key_id` in the manifest matches the key currently used in the Android app's `SecurityConstants`.
3.  **Hashed Filenames**: Ensure you are using the hashed filename (`-sha256.kpkg`) in the CDN, otherwise the app may fetch a cached, older version of the binary.
