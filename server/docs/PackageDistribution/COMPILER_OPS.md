# KoColor Package Compiler & Cryptography Guide

This operational manual details the end-to-end process for generating cryptographic keys, securing them, running the Rust Normalization Compiler, and deploying the signed packages to the CDN.

---

## 1. Cryptographic Key Management

The KoColor platform relies on an **Ed25519 Keypair**. The Private Key signs the packages, and the Public Key verifies them on the Android device.

### A. Generating the Keypair

Since your environment is already built on Rust, the safest way to generate a keypair is using the `ed25519-dalek` crate.

Create a temporary file in your Rust project (e.g., `src/bin/keygen.rs`):

```rust
use ed25519_dalek::SigningKey;
use rand::rngs::OsRng;

fn main() {
    let mut csprng = OsRng;
    let signing_key = SigningKey::generate(&mut csprng);
    let public_key = signing_key.verifying_key();

    println!("🔒 PRIVATE KEY (Keep Secret!):");
    println!("{}", hex::encode(signing_key.to_bytes()));
    
    println!("\n🔑 PUBLIC KEY (Ship in Android App):");
    println!("{}", hex::encode(public_key.as_bytes()));
}
```

Run it via terminal:

```bash
cargo run --bin keygen
```

### B. Where to Put the Private Key (The Rust Backend)

**Never commit the Private Key to version control.**

1. Create a `.env` file in the root of your Rust compiler project (`server/KoColor/`).
2. Add the private key:
```env
KOCOLOR_PRIVATE_KEY=your_64_character_hex_private_key_here
```
3. Add `.env` to your `.gitignore` file immediately.

### C. Where to Put the Public Key (The Android App)

The Public Key is safe to distribute. It should be compiled directly into the Android application.

1. Define it as a constant in your security module (`SecurityConstants.kt`):
```kotlin
// In SecurityConstants.kt
const val KOCOLOR_ROOT_PUBLIC_KEY = "your_64_character_hex_public_key_here"
```

---

## 2. Updating the Rust Compiler (Loading the Key)

Previously, the Rust compiler generated a new key every time it ran. It must be updated to load your static Private Key from the environment so the Android app can consistently verify the signatures.

Update your `src/main.rs` or bin script to load the key:

```rust
use ed25519_dalek::{Signer, SigningKey};
use std::env;
use std::fs;

fn main() {
    println!("🚀 Starting KoColor Normalization Compiler v2...");

    // 1. Load the Private Key from the environment
    let private_key_hex = env::var("KOCOLOR_PRIVATE_KEY")
        .expect("FATAL: KOCOLOR_PRIVATE_KEY environment variable not set.");
    
    let private_key_bytes = hex::decode(private_key_hex)
        .expect("FATAL: Private key is not valid Hex.");
    
    // Ensure it's exactly 32 bytes (Ed25519 standard)
    let signing_key = SigningKey::from_bytes(
        private_key_bytes.as_slice().try_into().unwrap()
    );

    // ... [Continue with mapping, hashing, signing, and saving] ...
}
```

---

## 3. Running the Compiler Pipeline

Whenever you need to update a starter pack (e.g., MAC releases a new shade), follow this workflow:

1. **Update the Source:** Ensure your Rust script is pointing to the latest source JSON endpoints or local CSV files.
2. **Increment the Version:** If the content changed, increment the `package_version` in your Rust script (e.g., `"1.0.0"` -> `"1.1.0"`).
3. **Compile:** Run the compiler from your terminal.
```bash
# Load the environment variable and run in release mode for maximum speed
export $(cat .env | xargs) && cargo run --release
```
4. **Verify Output:** Ensure the `manifest.json` and the individual pack files (e.g., `com.kocolor.pack.mac.core.json`) have been generated in your output directory.

---

## 4. Deploying to the CDN (Cloudflare)

Because the KoColor backend is a **Static-First** architecture, deployment simply means pushing the generated JSON and image files to your static host (Cloudflare Pages, Cloudflare R2, or GitHub Pages).

### Automated Deployment (Recommended)

You can automate this with a simple bash script:

```bash
#!/bin/bash
echo "📦 Building KoColor Packages..."
cargo run --release

echo "🚀 Deploying to Cloudflare..."
# Assuming you are using Cloudflare Wrangler CLI
wrangler pages deploy ./output_directory --project-name kocolor-cdn

echo "✅ Deployment Complete."
```

---

## 5. Security & Operational Checklist

Before running this in production, verify the following:

* [ ] **No Secrets in Git:** `KOCOLOR_PRIVATE_KEY` is not hardcoded anywhere in the `.rs` files and `.env` is in `.gitignore`.
* [ ] **Deterministic Builds:** Running the compiler twice with the exact same input data produces the *exact same* SHA-256 hash.
* [ ] **CDN Cache Headers:** Configure Cloudflare to serve `manifest.json` with a short cache TTL (e.g., 5 minutes) so the app detects updates quickly, but set the individual pack files to cache for a long time (e.g., 1 year) since they are immutable and versioned.
* [ ] **Schema Versioning:** If you ever add new fields to the `CanonicalPackItem` that older Android apps cannot parse, you must increment `schema_version` to `3` in the Rust compiler.
