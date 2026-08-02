use kocolor::inventory;
use kocolor::StarterPackResponse;
use std::fs::File;
use std::io::Write;

fn main() {
    let cosmetics = inventory::get_default_cosmetics();
    let clothing = inventory::get_default_clothing();

    let response = StarterPackResponse {
        version: 1,
        cosmetics,
        clothing,
    };

    let json_payload = serde_json::to_string_pretty(&response).expect("Failed to serialize");

    let mut file = File::create("starter-pack.json").expect("Failed to create file");
    file.write_all(json_payload.as_bytes()).expect("Failed to write JSON");

    println!("✅ Successfully generated modular starter-pack.json v1!");
}
