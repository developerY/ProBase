use kocolor::inventory::InventoryRegistry;
use kocolor::{StarterPackResponse, PackManifest, PackInfo, SignedPayloadEnvelope, SearchIndexEntry, CosmeticItem, ClothingItem};
use std::fs::File;
use std::io::Write;
use ed25519_dalek::{SigningKey, Signer};
use sha2::{Digest, Sha256};

fn main() {
    // Ed25519 Private Key - Read from environment variable
    let sk_hex = std::env::var("CDN_PRIVATE_KEY_HEX")
        .expect("ERROR: CDN_PRIVATE_KEY_HEX environment variable not set. Please set your Ed25519 private key hex.");

    let sk_bytes = hex::decode(sk_hex).expect("Invalid hex in CDN_PRIVATE_KEY_HEX");
    let signing_key = SigningKey::from_bytes(sk_bytes.as_slice().try_into().expect("Invalid private key format"));

    let full_cosmetics = InventoryRegistry::all_cosmetics();
    let full_clothing = InventoryRegistry::all_clothing();

    let mut search_index: Vec<SearchIndexEntry> = Vec::new();

    // 1. Generate Full Starter Pack
    let starter_pack = StarterPackResponse {
        version: 1,
        cosmetics: full_cosmetics.clone(),
        clothing: full_clothing.clone(),
    };
    let starter_sha256 = save_signed_payload("starter-pack.json", &starter_pack, &signing_key, "1.0.0");
    index_items(&mut search_index, &full_cosmetics, &full_clothing, "com.kocolor.pack.core");

    // 2. Generate Seasonal Winter Pack
    let (winter_cosm, winter_cloth) = InventoryRegistry::compose_pack(
        vec!["kc-cosm-005", "kc-cosm-004"],
        vec!["kc-cloth-001"]
    );
    let winter_pack = StarterPackResponse {
        version: 1,
        cosmetics: winter_cosm.clone(),
        clothing: winter_cloth.clone(),
    };
    let winter_sha256 = save_signed_payload("winter-essentials.json", &winter_pack, &signing_key, "1.0.0");
    index_items(&mut search_index, &winter_cosm, &winter_cloth, "com.kocolor.pack.winter2026");

    // 3. Generate Spring Prep Kit
    let (spring_cosm, _) = InventoryRegistry::compose_pack(
        vec!["kc-cosm-001", "kc-cosm-002"],
        vec![]
    );
    let spring_pack = StarterPackResponse {
        version: 1,
        cosmetics: spring_cosm.clone(),
        clothing: vec![],
    };
    let spring_sha256 = save_signed_payload("spring-prep.json", &spring_pack, &signing_key, "1.0.0");
    index_items(&mut search_index, &spring_cosm, &vec![], "com.kocolor.pack.spring2026");

    // 4. Save Search Index
    let search_index_json = serde_json::to_string_pretty(&search_index).expect("Failed to serialize search index");
    let mut search_file = File::create("search_index.json").expect("Failed to create search_index.json");
    search_file.write_all(search_index_json.as_bytes()).expect("Failed to write search_index.json");

    // 5. Generate the Manifest
    let manifest = PackManifest {
        packs: vec![
            PackInfo {
                id: "com.kocolor.pack.core".to_string(),
                name: "Core Collection".to_string(),
                publisher: "KoColor Official".to_string(),
                description: "The foundational high-fidelity product library for all users.".to_string(),
                version: 1,
                pack_type: "STARTER_PACK".to_string(),
                endpoint: "starter-pack.json".to_string(),
                item_count: (full_cosmetics.len() + full_clothing.len()) as u32,
                size_bytes: Some(1200000),
                sha256: Some(starter_sha256),
                hero_image_url: None,
                expires_at: None,
            },
            PackInfo {
                id: "com.kocolor.pack.winter2026".to_string(),
                name: "Winter 2026 Trend Kit".to_string(),
                publisher: "KoColor Official".to_string(),
                description: "Curated seasonal picks for Winter color profiles.".to_string(),
                version: 1,
                pack_type: "SAMPLE_PACK".to_string(),
                endpoint: "winter-essentials.json".to_string(),
                item_count: (winter_cosm.len() + winter_cloth.len()) as u32,
                size_bytes: Some(450000),
                sha256: Some(winter_sha256),
                hero_image_url: None,
                expires_at: None,
            },
            PackInfo {
                id: "com.kocolor.pack.spring2026".to_string(),
                name: "Spring Skin Prep".to_string(),
                publisher: "KoColor Official".to_string(),
                description: "Revitalizing routine for the new season.".to_string(),
                version: 1,
                pack_type: "SAMPLE_PACK".to_string(),
                endpoint: "spring-prep.json".to_string(),
                item_count: spring_cosm.len() as u32,
                size_bytes: Some(320000),
                sha256: Some(spring_sha256),
                hero_image_url: None,
                expires_at: Some(1748736000000),
            },
        ],
    };

    save_signed_payload("manifest.json", &manifest, &signing_key, "1.0.0");

    println!("✅ Generated and SIGNED: starter-pack.json, winter-essentials.json, spring-prep.json, and manifest.json");
    println!("✅ Generated discovery index: search_index.json");
}

fn save_signed_payload<T: serde::Serialize>(
    filename: &str,
    payload: &T,
    signing_key: &SigningKey,
    package_version: &str
) -> String {
    let payload_json = serde_json::to_value(payload).expect("Failed to serialize to value");
    let payload_string = serde_json::to_string(&payload_json).expect("Failed to serialize to string");

    // 1. Calculate SHA-256 for the manifest
    let mut hasher = Sha256::new();
    hasher.update(payload_string.as_bytes());
    let sha256_hex = hex::encode(hasher.finalize());

    // 2. Sign the raw JSON string with Ed25519
    let signature = signing_key.sign(payload_string.as_bytes());
    let signature_hex = hex::encode(signature.to_bytes());

    let envelope = SignedPayloadEnvelope {
        data: payload_json,
        signature: signature_hex,
        package_version: package_version.to_string(),
        schema_version: 2, // Android Zero-Trust requirement
    };

    let final_json = serde_json::to_string_pretty(&envelope).expect("Failed to serialize envelope");
    let mut file = File::create(filename).expect("Failed to create file");
    file.write_all(final_json.as_bytes()).expect("Failed to write JSON");

    sha256_hex
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
