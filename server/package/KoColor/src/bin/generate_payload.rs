use kocolor::{StarterPackResponse, PackManifest, PackInfo, SignedPayloadEnvelope, SearchIndexEntry, CosmeticItem, ClothingItem};
use std::fs::{self, File};
use std::io::Write;
use std::path::Path;
use ed25519_dalek::{SigningKey, Signer};
use sha2::{Digest, Sha256};
use chrono::Utc;

const DIST_DIR: &str = "dist";
const INPUT_DIR: &str = "input_packs"; // Target directory for all source JSON files

fn main() {
    // 0. Ensure required directories exist
    fs::create_dir_all(DIST_DIR).expect("Failed to create dist directory");
    fs::create_dir_all(INPUT_DIR).expect("Failed to create input_packs directory");

    // Ed25519 Private Key - Read from environment variable for security
    let sk_hex = std::env::var("CDN_PRIVATE_KEY_HEX")
        .expect("ERROR: CDN_PRIVATE_KEY_HEX environment variable not set. Please set your Ed25519 private key hex.");

    let sk_bytes = hex::decode(sk_hex).expect("Invalid hex in CDN_PRIVATE_KEY_HEX");
    let signing_key = SigningKey::from_bytes(sk_bytes.as_slice().try_into().expect("Invalid private key format"));

    let mut search_index: Vec<SearchIndexEntry> = Vec::new();
    let mut compiled_packs: Vec<PackInfo> = Vec::new();

    let compiler_version = "kocolor-compiler 1.5.0".to_string();
    let generated_at = Utc::now().to_rfc3339();
    let key_id = "kocolor-root-v1".to_string();

    // 1. Read all files in the input directory
    let paths = fs::read_dir(INPUT_DIR).expect("Failed to read input_packs directory");

    for path in paths {
        let entry = path.expect("Failed to read directory entry");
        let path_buf = entry.path();

        // 2. Process only valid .json files
        if path_buf.is_file() && path_buf.extension().and_then(|s| s.to_str()) == Some("json") {
            let file_stem = path_buf.file_stem().and_then(|s| s.to_str()).unwrap_or("unknown_pack");

            // 3. Derive Manifest Metadata from the filename
            let (pack_id, pack_name) = if file_stem == "core_collection" {
                ("com.kocolor.pack.core".to_string(), "KoColor Core Collection".to_string())
            } else {
                (
                    format!("com.kocolor.pack.{}", file_stem.replace("_", "-")),
                    file_stem.replace("_", " ").to_uppercase()
                )
            };

            let description = if file_stem == "core_collection" {
                "The foundational high-fidelity product library for all users.".to_string()
            } else {
                format!("Automated compilation of {}.json", file_stem)
            };

            println!("⚙️  Compiling: {}", path_buf.display());

            // 4. Parse JSON and validate KCPS v1
            let json_content = fs::read_to_string(&path_buf).expect("Failed to read JSON file");
            let payload: StarterPackResponse = serde_json::from_str(&json_content)
                .unwrap_or_else(|e| panic!("❌ Invalid KCPS JSON in {}: {}", file_stem, e));

            // STRICT VERSION 1 ENFORCEMENT
            if payload.schema_version != 1 {
                panic!("❌ Schema Mismatch in {}.json: Expected v1, found v{}", file_stem, payload.schema_version);
            }

            // 5. Compile, Compress, Hash, and Sign
            let pack_info = compile_and_sign_pack(
                &pack_id,
                &pack_name,
                &description,
                "KoColor Official",
                "STANDARD_PACK",
                &payload,
                &payload.cosmetics,
                &payload.clothing,
                &signing_key,
                "1.0.0"
            );

            // 6. Append to index and manifest collections
            index_items(&mut search_index, &payload.cosmetics, &payload.clothing, &pack_id);
            compiled_packs.push(pack_info);
        }
    }

    // 7. Save Search Index
    let search_index_json = serde_json::to_string_pretty(&search_index).expect("Failed to serialize search index");
    let search_path = format!("{}/search_index.json", DIST_DIR);
    let mut search_file = File::create(search_path).expect("Failed to create search_index.json");
    search_file.write_all(search_index_json.as_bytes()).expect("Failed to write search_index.json");

    // 8. Generate and Save the Root Manifest
    let manifest = PackManifest {
        manifest_version: 1,
        generated_at,
        compiler_version,
        key_id,
        packs: compiled_packs,
    };

    let manifest_path = format!("{}/manifest.json", DIST_DIR);
    save_signed_manifest(&manifest_path, &manifest, &signing_key, "1.0.0");

    println!("✅ Generated and SIGNED all binary .kpkg packages and manifest.json in /{}", DIST_DIR);
    println!("✅ Generated discovery index: search_index.json in /{}", DIST_DIR);
}

fn compile_and_sign_pack<T: serde::Serialize>(
    id: &str,
    name: &str,
    description: &str,
    publisher: &str,
    pack_type: &str,
    payload: &T,
    cosmetics: &Vec<CosmeticItem>,
    clothing: &Vec<ClothingItem>,
    signing_key: &SigningKey,
    _package_version: &str
) -> PackInfo {
    // 1. Deterministic Minified JSON Serialization
    let json_bytes = serde_json::to_vec(payload).expect("Failed to serialize to JSON");
    let uncompressed_size = json_bytes.len() as u64;

    // 2. Zstandard Compression (Level 3 - mobile optimized)
    let compressed_bytes = zstd::stream::encode_all(json_bytes.as_slice(), 3).expect("Failed to compress with zstd");
    let compressed_size = compressed_bytes.len() as u64;

    // 3. Calculate SHA-256 strictly on COMPRESSED bytes
    let mut hasher = Sha256::new();
    hasher.update(&compressed_bytes);
    let sha256_hex = hex::encode(hasher.finalize());

    // 4. Sign COMPRESSED bytes with Ed25519
    let signature = signing_key.sign(&compressed_bytes);
    let signature_hex = hex::encode(signature.to_bytes());

    // 5. Build Preview Projection
    let mut preview_items: Vec<kocolor::PreviewItem> = Vec::new();

    // Add cosmetics to preview
    for item in cosmetics {
        let shade = item.shade_name.clone().unwrap_or_else(|| "Standard".to_string());
        let desc = format!(
            "{} • {} • {} COVERAGE",
            shade,
            item.finish.as_deref().unwrap_or("NATURAL"),
            item.coverage.as_deref().unwrap_or("MEDIUM")
        );
        preview_items.push(kocolor::PreviewItem {
            name: item.name.clone(),
            description: desc,
        });
    }

    // Add clothing to preview if room
    for item in clothing {
        let desc = format!(
            "{} • {} FORMALITY",
            item.material.as_deref().unwrap_or("Mixed Fiber"),
            item.formality.as_deref().unwrap_or("CASUAL")
        );
        preview_items.push(kocolor::PreviewItem {
            name: item.name.clone(),
            description: desc,
        });
    }

    // Cap to 10 items for manifest efficiency
    let actual_count = preview_items.len();
    if preview_items.len() > 10 {
        preview_items.truncate(10);
        preview_items.push(kocolor::PreviewItem {
            name: "...".to_string(),
            description: format!("And {} more high-fidelity items", actual_count - 10),
        });
    }

    // 6. Save as immutable binary artifact
    let filename = format!("{}-{}.kpkg", id, sha256_hex);
    let filepath = format!("{}/{}", DIST_DIR, filename);
    let mut file = File::create(&filepath).expect("Failed to create kpkg file");
    file.write_all(&compressed_bytes).expect("Failed to write kpkg binary");

    PackInfo {
        id: id.to_string(),
        name: name.to_string(),
        publisher: publisher.to_string(),
        description: description.to_string(),
        version: 1,
        pack_type: pack_type.to_string(),
        endpoint: filename,
        item_count: (cosmetics.len() + clothing.len()) as u32,
        compressed_size_bytes: compressed_size,
        uncompressed_size_bytes: uncompressed_size,
        sha256: sha256_hex,
        signature: signature_hex,
        compression_algorithm: "zstd".to_string(),
        hash_algorithm: "sha256".to_string(),
        hash_encoding: "hex-lowercase".to_string(),
        signature_algorithm: "ed25519".to_string(),
        signature_encoding: "hex".to_string(),
        package_format_version: 1,
        schema_version: 1,
        encryption: "none".to_string(),
        hero_image_url: None,
        expires_at: if id.contains("spring") { Some(Utc::now().timestamp_millis() as u64 + 7776000000) } else { None },
        preview_items,
    }
}

fn save_signed_manifest(
    filename: &str,
    manifest: &PackManifest,
    signing_key: &SigningKey,
    package_version: &str
) {
    let manifest_json = serde_json::to_value(manifest).expect("Failed to serialize manifest");
    let manifest_string = serde_json::to_string(&manifest_json).expect("Failed to serialize manifest string");

    let signature = signing_key.sign(manifest_string.as_bytes());
    let signature_hex = hex::encode(signature.to_bytes());

    let envelope = SignedPayloadEnvelope {
        data: manifest_json,
        signature: signature_hex,
        package_version: package_version.to_string(),
        schema_version: 1,
    };

    let final_json = serde_json::to_string(&envelope).expect("Failed to serialize manifest envelope");
    let mut file = File::create(filename).expect("Failed to create manifest file");
    file.write_all(final_json.as_bytes()).expect("Failed to write manifest JSON");
}

fn index_items(
    index: &mut Vec<SearchIndexEntry>,
    cosmetics: &Vec<CosmeticItem>,
    clothing: &Vec<ClothingItem>,
    pack_id: &str
) {
    for item in cosmetics {
        index.push(SearchIndexEntry {
            id: item.id.clone(),
            term: item.name.clone(),
            brand: item.brand.clone(),
            pack_id: pack_id.to_string(),
        });
    }
    for item in clothing {
        index.push(SearchIndexEntry {
            id: item.id.clone(),
            term: item.name.clone(),
            brand: item.brand.clone(),
            pack_id: pack_id.to_string(),
        });
    }
}
