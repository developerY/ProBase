use kocolor::inventory::InventoryRegistry;
use kocolor::{StarterPackResponse, PackManifest, PackInfo, SignedPayloadEnvelope};
use std::fs::File;
use std::io::Write;
use p256::ecdsa::{SigningKey, signature::Signer};
use base64::{Engine as _, engine::general_purpose::STANDARD as BASE64};

fn main() {
    // SECP256R1 Private Key (Placeholder - in production use environment variables)
    let sk_hex = "41f26f634582f3c7e6c4349377833a6b83f3e1b7c02b37a1a1f0a1f0a1f0a1f0";
    let signing_key = SigningKey::from_slice(&hex::decode(sk_hex).unwrap()).expect("Invalid private key");

    let full_cosmetics = InventoryRegistry::all_cosmetics();
    let full_clothing = InventoryRegistry::all_clothing();

    // 1. Generate Full Starter Pack
    let starter_pack = StarterPackResponse {
        version: 1,
        cosmetics: full_cosmetics.clone(),
        clothing: full_clothing.clone(),
    };
    save_signed_payload("starter-pack.json", &starter_pack, &signing_key);

    // 2. Generate Seasonal Winter Pack
    let (winter_cosm, winter_cloth) = InventoryRegistry::compose_pack(
        vec!["kc-cosm-005", "kc-cosm-014"],
        vec!["kc-cloth-001"]
    );
    let winter_pack = StarterPackResponse {
        version: 1,
        cosmetics: winter_cosm.clone(),
        clothing: winter_cloth.clone(),
    };
    save_signed_payload("winter-essentials.json", &winter_pack, &signing_key);

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
    save_signed_payload("spring-prep.json", &spring_pack, &signing_key);

    // 4. Generate the Manifest
    let manifest = PackManifest {
        packs: vec![
            PackInfo {
                id: "starter_pack_v1".to_string(),
                name: "Core Collection".to_string(),
                description: "The foundational high-fidelity product library for all users.".to_string(),
                version: 1,
                pack_type: "STARTER_PACK".to_string(),
                endpoint: "starter-pack.json".to_string(),
                item_count: (full_cosmetics.len() + full_clothing.len()) as u32,
                size_bytes: Some(1200000),
                hash: Some("sha256:abc123full".to_string()),
                hero_image_url: None,
                expires_at: None,
            },
            PackInfo {
                id: "winter_2026_kit".to_string(),
                name: "Winter 2026 Trend Kit".to_string(),
                description: "Curated seasonal picks for Winter color profiles.".to_string(),
                version: 1,
                pack_type: "SAMPLE_PACK".to_string(),
                endpoint: "winter-essentials.json".to_string(),
                item_count: (winter_cosm.len() + winter_cloth.len()) as u32,
                size_bytes: Some(450000),
                hash: Some("sha256:xyz456winter".to_string()),
                hero_image_url: None,
                expires_at: None,
            },
            PackInfo {
                id: "spring_prep_2026".to_string(),
                name: "Spring Skin Prep".to_string(),
                description: "Revitalizing routine for the new season.".to_string(),
                version: 1,
                pack_type: "SAMPLE_PACK".to_string(),
                endpoint: "spring-prep.json".to_string(),
                item_count: spring_cosm.len() as u32,
                size_bytes: Some(320000),
                hash: Some("sha256:def789spring".to_string()),
                hero_image_url: None,
                expires_at: Some(1748736000000),
            },
        ],
    };

    save_signed_payload("manifest.json", &manifest, &signing_key);

    println!("✅ Generated and SIGNED: starter-pack.json, winter-essentials.json, spring-prep.json, and manifest.json");
}

fn save_signed_payload<T: serde::Serialize>(filename: &str, payload: &T, signing_key: &SigningKey) {
    let payload_json = serde_json::to_value(payload).expect("Failed to serialize to value");
    let payload_string = serde_json::to_string(&payload_json).expect("Failed to serialize to string");

    // Sign the raw JSON string
    let signature: p256::ecdsa::Signature = signing_key.sign(payload_string.as_bytes());
    let signature_base64 = BASE64.encode(signature.to_der());

    let envelope = SignedPayloadEnvelope {
        signature: signature_base64,
        payload: payload_json,
    };

    let final_json = serde_json::to_string_pretty(&envelope).expect("Failed to serialize envelope");
    let mut file = File::create(filename).expect("Failed to create file");
    file.write_all(final_json.as_bytes()).expect("Failed to write JSON");
}
