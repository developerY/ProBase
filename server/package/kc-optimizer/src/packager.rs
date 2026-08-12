use ed25519_dalek::{Signer, SigningKey, Signature};
use sha2::{Digest, Sha256};
use std::fs;
use std::path::Path;
use zstd::stream::encode_all;

/// Compresses the KCPS JSON payload using Zstd, writes the .kpkg binary,
/// computes its SHA-256 hash, and signs it with an Ed25519 private key.
/// Returns (hash_hex, signature_hex, uncompressed_size_bytes, compressed_size_bytes).
pub fn seal_package(
    json_bytes: &[u8],
    package_id: &str,
    signing_key: &SigningKey,
    dist_dir: &Path,
) -> (String, String, u64, u64) {
    let uncompressed_size = json_bytes.len() as u64;

    // 1. Zstd Compression
    // We use level 19 (maximum) because we are computing at compile-time.
    // This ruthlessly shrinks the payload for mobile downloads.
    let compressed_bytes = encode_all(json_bytes, 19)
        .expect("❌ Zstd compression failed");

    // 2. Write the .kpkg binary artifact
    let kpkg_filename = format!("{}.kpkg", package_id);
    let kpkg_path = dist_dir.join(&kpkg_filename);
    fs::write(&kpkg_path, &compressed_bytes)
        .unwrap_or_else(|err| panic!("❌ Failed to write {}: {}", kpkg_filename, err));

    // 3. Generate SHA-256 Hash
    let mut hasher = Sha256::new();
    hasher.update(&compressed_bytes);
    let hash_result = hasher.finalize();
    let hash_hex = hex::encode(hash_result);

    // 4. Cryptographic Signing (Ed25519)
    // We sign the compressed bytes to guarantee absolute integrity on the CDN
    let signature: Signature = signing_key.sign(&compressed_bytes);
    let sig_hex = hex::encode(signature.to_bytes());

    (hash_hex, sig_hex, uncompressed_size, compressed_bytes.len() as u64)
}

/// Signs the raw byte array with an Ed25519 private key.
pub fn sign_data(data: &[u8], signing_key: &SigningKey) -> String {
    let signature: Signature = signing_key.sign(data);
    hex::encode(signature.to_bytes())
}
