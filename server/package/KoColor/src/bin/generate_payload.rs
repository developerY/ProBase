use kocolor::inventory::InventoryRegistry;
use kocolor::{StarterPackResponse, PackManifest, PackInfo, SignedPayloadEnvelope, SearchIndexEntry, CosmeticItem, ClothingItem};
use std::fs::{self, File};
use std::io::Write;
use ed25519_dalek::{SigningKey, Signer};
use sha2::{Digest, Sha256};
use chrono::Utc;

const DIST_DIR: &str = "dist";

fn main() {
    // 0. Ensure distribution directory exists
    fs::create_dir_all(DIST_DIR).expect("Failed to create dist directory");

    // Ed25519 Private Key - Read from environment variable for security
    let sk_hex = std::env::var("CDN_PRIVATE_KEY_HEX")
        .expect("ERROR: CDN_PRIVATE_KEY_HEX environment variable not set. Please set your Ed25519 private key hex.");

    let sk_bytes = hex::decode(sk_hex).expect("Invalid hex in CDN_PRIVATE_KEY_HEX");
    let signing_key = SigningKey::from_bytes(sk_bytes.as_slice().try_into().expect("Invalid private key format"));

    let full_cosmetics = InventoryRegistry::all_cosmetics();
    let full_clothing = InventoryRegistry::all_clothing();

    let mut search_index: Vec<SearchIndexEntry> = Vec::new();
    let compiler_version = "kocolor-compiler 1.5.0".to_string();
    let generated_at = Utc::now().to_rfc3339();
    let key_id = "kocolor-root-v1".to_string();

    // 1. Generate Full Starter Pack
    let starter_pack = StarterPackResponse {
        schema_version: 2,
        cosmetics: full_cosmetics.clone(),
        clothing: full_clothing.clone(),
    };
    let starter_info = compile_and_sign_pack(
        "com.kocolor.pack.core",
        "Core Collection",
        "The foundational high-fidelity product library for all users.",
        "KoColor Official",
        "STARTER_PACK",
        &starter_pack,
        &signing_key,
        "1.0.0"
    );
    index_items(&mut search_index, &full_cosmetics, &full_clothing, "com.kocolor.pack.core");

    // 2. Generate Seasonal Winter Pack
    let (winter_cosm, winter_cloth) = InventoryRegistry::compose_pack(
        vec!["kc-cosm-005", "kc-cosm-004"],
        vec!["kc-cloth-001"]
    );
    let winter_pack = StarterPackResponse {
        schema_version: 2,
        cosmetics: winter_cosm.clone(),
        clothing: winter_cloth.clone(),
    };
    let winter_info = compile_and_sign_pack(
        "com.kocolor.pack.winter2026",
        "Winter 2026 Trend Kit",
        "Curated seasonal picks for Winter color profiles.",
        "KoColor Official",
        "SAMPLE_PACK",
        &winter_pack,
        &signing_key,
        "1.0.0"
    );
    index_items(&mut search_index, &winter_cosm, &winter_cloth, "com.kocolor.pack.winter2026");

    // 3. Generate Spring Prep Kit
    let (spring_cosm, _) = InventoryRegistry::compose_pack(
        vec!["kc-cosm-001", "kc-cosm-002"],
        vec![]
    );
    let spring_pack = StarterPackResponse {
        schema_version: 2,
        cosmetics: spring_cosm.clone(),
        clothing: vec![],
    };
    let spring_info = compile_and_sign_pack(
        "com.kocolor.pack.spring2026",
        "Spring Skin Prep",
        "Revitalizing routine for the new season.",
        "KoColor Official",
        "SAMPLE_PACK",
        &spring_pack,
        &signing_key,
        "1.0.0"
    );
    index_items(&mut search_index, &spring_cosm, &vec![], "com.kocolor.pack.spring2026");

    // 4. Save Search Index
    let search_index_json = serde_json::to_string_pretty(&search_index).expect("Failed to serialize search index");
    let search_path = format!("{}/search_index.json", DIST_DIR);
    let mut search_file = File::create(search_path).expect("Failed to create search_index.json");
    search_file.write_all(search_index_json.as_bytes()).expect("Failed to write search_index.json");

    // 5. Generate the Manifest
    let manifest = PackManifest {
        manifest_version: 1,
        generated_at,
        compiler_version,
        key_id,
        packs: vec![starter_info, winter_info, spring_info],
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
    signing_key: &SigningKey,
    _package_version: &str
) -> PackInfo {
    // 1. Deterministic Minified JSON Serialization
    // Note: Rust Normalization Compiler is the canonical serializer.
    // The implementation MUST ensure deterministic field ordering, encoding, and escaping.
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

    // 5. Save as immutable binary artifact
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
        item_count: match serde_json::to_value(payload).unwrap() {
            serde_json::Value::Object(map) => {
                let mut count = 0;
                if let Some(serde_json::Value::Array(v)) = map.get("cosmetics") { count += v.len(); }
                if let Some(serde_json::Value::Array(v)) = map.get("clothing") { count += v.len(); }
                count as u32
            },
            _ => 0
        },
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
        schema_version: 2,
        encryption: "none".to_string(),
        hero_image_url: None,
        expires_at: if id.contains("spring") { Some(Utc::now().timestamp_millis() as u64 + 7776000000) } else { None },
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
        schema_version: 2,
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
            brand: item.brand.clone().unwrap_or("Generic".to_string()),
            pack_id: pack_id.to_string(),
        });
    }
}
