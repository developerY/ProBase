use serde::Serialize;
use std::fs::File;
use std::io::Write;

#[derive(Serialize)]
struct StarterPackResponse {
    version: u32,
    cosmetics: Vec<CosmeticItem>,
    clothing: Vec<ClothingItem>,
}

#[derive(Serialize)]
struct CosmeticItem {
    id: String,
    name: String,
    macro_category: String,
    micro_category: String,

    // Level 3 Professional Facets (The Intelligence Layer)
    formulation: String,
    chemistry: String,
    finish: String,
    coverage: String,
    temperature: String,

    // UI Visuals
    color_hex: String,
    image_url: String,
}

#[derive(Serialize)]
struct ClothingItem {
    id: String,
    name: String,
    macro_category: String,
    micro_category: String,
    color_hex: String,
    image_url: String,
}

fn main() {
    let cosmetics = vec![
        CosmeticItem {
            id: "cosmetic_001".to_string(),
            name: "KoColor Signature Crimson Lip Color".to_string(),
            macro_category: "Lips".to_string(),
            micro_category: "Lipstick".to_string(),
            formulation: "Cream".to_string(),
            chemistry: "Oil".to_string(),
            finish: "Satin".to_string(),
            coverage: "Full".to_string(),
            temperature: "Neutral".to_string(),
            color_hex: "#5A1827".to_string(),
            image_url: "https://cdn.kocolor.com/inventory/assets/image_07157d.webp".to_string(),
        },
        CosmeticItem {
            id: "cosmetic_002".to_string(),
            name: "KoColor Velvet Canvas Primer".to_string(),
            macro_category: "Skincare & Prep".to_string(),
            micro_category: "Primer".to_string(),
            formulation: "Gel".to_string(),
            chemistry: "Silicone".to_string(),
            finish: "Matte".to_string(),
            coverage: "Sheer".to_string(),
            temperature: "Neutral".to_string(),
            color_hex: "#FFFFFF".to_string(),
            image_url: "https://cdn.kocolor.com/inventory/assets/image_velvet_primer.webp".to_string(),
        },
        CosmeticItem {
            id: "cosmetic_003".to_string(),
            name: "KoColor Sculpt & Define Contour Powder".to_string(),
            macro_category: "Color & Dimension".to_string(),
            micro_category: "Contour".to_string(),
            formulation: "Powder".to_string(),
            chemistry: "Silicone".to_string(),
            finish: "Matte".to_string(),
            coverage: "Buildable".to_string(),
            temperature: "Cool".to_string(),
            color_hex: "#4A3728".to_string(),
            image_url: "https://cdn.kocolor.com/inventory/assets/image_contour_powder.webp".to_string(),
        },
    ];

    let response = StarterPackResponse {
        version: 1,
        cosmetics,
        clothing: vec![], // Placeholder for clothing items
    };

    let json_payload = serde_json::to_string_pretty(&response).expect("Failed to serialize");

    let mut file = File::create("starter-pack.json").expect("Failed to create file");
    file.write_all(json_payload.as_bytes()).expect("Failed to write JSON");

    println!("✅ Successfully generated starter-pack.json v1 with Glow Archive Taxonomy!");
}
