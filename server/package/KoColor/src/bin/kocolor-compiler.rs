use std::env;
use std::fs;
use serde::{Deserialize, Serialize};

const KCPS_VERSION: u32 = 1;

#[derive(Serialize, Deserialize, Debug)]
struct KcpsPayload {
    schema_version: u32,
    cosmetics: Vec<CosmeticItem>,
    clothing: Vec<ClothingItem>,
}

#[derive(Serialize, Deserialize, Debug)]
struct CosmeticItem {
    id: String,
    name: String,
    brand: String,
    macro_category: String,
    micro_category: String,
    color_hex: String,
    shade_name: Option<String>,
    image_url: String,
    thumbnail_url: String,
    price: Option<f64>,
    notes: Option<String>,
    formulation: Option<String>,
    chemistry_base: Option<String>,
    finish: Option<String>,
    coverage: Option<String>,
    temperature: Option<String>,
    volume: Option<String>,
    pao_months: Option<u32>,
    expiry_date: Option<u64>,
    instructions: Option<String>,
    ingredients: Vec<String>,
    allergens: Vec<String>,
    is_vegan: Option<bool>,
    is_cruelty_free: Option<bool>,
    fda_data_verified: bool,
}

#[derive(Serialize, Deserialize, Debug)]
struct ClothingItem {
    id: String,
    name: String,
    brand: Option<String>,
    macro_category: String,
    micro_category: String,
    color_hex: String,
    shade_name: Option<String>,
    image_url: String,
    thumbnail_url: String,
    price: Option<f64>,
    notes: Option<String>,
    formality: Option<String>,
    material: Option<String>,
}

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() < 3 {
        eprintln!("Usage: kocolor-compiler build <package-id> <input-json-path>");
        std::process::exit(1);
    }

    let command = &args[1];
    if command != "build" {
        eprintln!("❌ Unknown command. Use 'build'.");
        std::process::exit(1);
    }

    let pack_id = &args[2];
    let input_path = &args[3];

    println!("⚙️  Compiling KoColor Package: {}", pack_id);

    // 1. Read the raw partner JSON
    let raw_json = fs::read_to_string(input_path)
        .expect("❌ Failed to read the input JSON file.");

    // 2. Parse and Validate KCPS v1
    let payload: KcpsPayload = serde_json::from_str(&raw_json)
        .expect("❌ Invalid JSON: Fails structure validation.");

    if payload.schema_version != KCPS_VERSION {
        eprintln!("❌ Schema Mismatch: Expected v{}, found v{}", KCPS_VERSION, payload.schema_version);
        std::process::exit(1);
    }

    println!("✅ KCPS Version 1 Validation Passed.");

    // 3. Deterministic Serialization
    // We NEVER compress raw_json. We serialize the validated struct to ensure stable hashes.
    let canonical_bytes = serde_json::to_vec(&payload)
        .expect("❌ Failed to serialize canonical payload.");

    // 4. Compress (Placeholder for actual zstd logic if needed, but following prompt logic)
    let compressed_bytes = canonical_bytes.clone();

    // 5. Hash (Full SHA-256)
    // Note: In a real implementation we would use sha2 crate
    let full_hash_hex = "f49b8a1c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4a3b2c1d0e".to_string(); // Placeholder

    // 6. Sign
    // Note: In a real implementation we would use ed25519_dalek crate
    let signature_hex = "3045022100...".to_string(); // Placeholder

    // 7. Output .kpkg using FULL hash
    let output_filename = format!("com.kocolor.pack.{}-{}.kpkg", pack_id, full_hash_hex);

    fs::write(&output_filename, &compressed_bytes)
        .expect("❌ Failed to write .kpkg binary.");

    // 8. Generate Manifest Entry
    let manifest_entry = serde_json::json!({
        "package_id": format!("com.kocolor.pack.{}", pack_id),
        "endpoint": format!("/packs/{}", output_filename),
        "sha256": full_hash_hex,
        "signature": signature_hex,
        "signature_algorithm": "ed25519",
        "signature_encoding": "hex",
        "compressed_size_bytes": compressed_bytes.len(),
        "uncompressed_size_bytes": canonical_bytes.len(),
        "package_format_version": 1,
        "schema_version": KCPS_VERSION
    });

    let manifest_json = serde_json::to_string_pretty(&manifest_entry).unwrap();
    println!("🚀 Success! Generated: {}\n\nManifest Entry:\n{}", output_filename, manifest_json);
}
