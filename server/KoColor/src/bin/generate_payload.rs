use kocolor::inventory::InventoryRegistry;
use kocolor::{StarterPackResponse, PackManifest, PackInfo};
use std::fs::File;
use std::io::Write;

fn main() {
    let full_cosmetics = InventoryRegistry::all_cosmetics();
    let full_clothing = InventoryRegistry::all_clothing();

    // 1. Generate Full Starter Pack
    let starter_pack = StarterPackResponse {
        version: 1,
        cosmetics: full_cosmetics.clone(),
        clothing: full_clothing.clone(),
    };
    save_payload("starter-pack.json", &starter_pack);

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
    save_payload("winter-essentials.json", &winter_pack);

    // 3. Generate the Manifest
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
            },
            PackInfo {
                id: "winter_2026_kit".to_string(),
                name: "Winter 2026 Trend Kit".to_string(),
                description: "Curated seasonal picks for Winter color profiles.".to_string(),
                version: 1,
                pack_type: "SAMPLE_PACK".to_string(),
                endpoint: "winter-essentials.json".to_string(),
                item_count: (winter_cosm.len() + winter_cloth.len()) as u32,
            },
        ],
    };

    let manifest_json = serde_json::to_string_pretty(&manifest).expect("Failed to serialize manifest");
    let mut file = File::create("manifest.json").expect("Failed to create manifest.json");
    file.write_all(manifest_json.as_bytes()).expect("Failed to write manifest");

    println!("✅ Generated: starter-pack.json, winter-essentials.json, and manifest.json");
}

fn save_payload(filename: &str, response: &StarterPackResponse) {
    let json_payload = serde_json::to_string_pretty(response).expect("Failed to serialize");
    let mut file = File::create(filename).expect("Failed to create file");
    file.write_all(json_payload.as_bytes()).expect("Failed to write JSON");
}
