use kocolor::inventory::InventoryRegistry;
use kocolor::StarterPackResponse;
use std::fs::File;
use std::io::Write;

fn main() {
    generate_starter_pack();
    generate_sample_winter_pack();
}

fn generate_starter_pack() {
    let cosmetics = InventoryRegistry::all_cosmetics();
    let clothing = InventoryRegistry::all_clothing();

    let response = StarterPackResponse {
        version: 1,
        cosmetics,
        clothing,
    };

    save_payload("starter-pack.json", &response);
    println!("✅ Generated: starter-pack.json (Full Catalog)");
}

fn generate_sample_winter_pack() {
    // Compose a specific pack for Winter users
    let (cosmetics, clothing) = InventoryRegistry::compose_pack(
        vec!["kc-cosm-005", "kc-cosm-002"], // Lip Stain and C Serum
        vec!["kc-cloth-001"]               // Silk Blouse
    );

    let response = StarterPackResponse {
        version: 1,
        cosmetics,
        clothing,
    };

    save_payload("sample-winter-pack.json", &response);
    println!("✅ Generated: sample-winter-pack.json (Curated Winter)");
}

fn save_payload(filename: &str, response: &StarterPackResponse) {
    let json_payload = serde_json::to_string_pretty(response).expect("Failed to serialize");
    let mut file = File::create(filename).expect("Failed to create file");
    file.write_all(json_payload.as_bytes()).expect("Failed to write JSON");
}
