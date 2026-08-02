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
    brand: String,
    macro_category: String,
    micro_category: String,

    // Level 3 Professional Facets
    formulation: String,
    chemistry_base: String,
    finish: String,
    coverage: String,
    temperature: String,

    // UI Visuals
    color_hex: String,
    shade_name: Option<String>,
    image_url: String,
    notes: Option<String>,
    instructions: Option<String>,

    // Professional Inventory & Logistics
    batch_code: Option<String>,
    pao_months: Option<u32>,
    price: Option<f64>,
    volume: Option<String>,
    expiry_date: Option<u64>,

    // Algorithmic & AI Insights
    hero_ingredient: Option<String>,
    skin_compatibility: Option<String>,
    contains_fragrance: Option<bool>,
    ingredients: Vec<String>,
    allergens: Vec<String>,

    // Sustainability & Eco-Impact
    eco_score: Option<String>,
    is_vegan: Option<bool>,
    is_cruelty_free: Option<bool>,
    recycling_instructions: Option<String>,

    // Ritual Context
    ritual_placement: Option<String>,

    // FDA & Clinical Safety
    fda_recall_status: Option<String>,
    fda_adverse_event_count: u32,
    fda_clinical_warnings: Vec<String>,
    fda_top_reactions: Vec<String>,
    fda_active_ingredients: Vec<String>,
    is_fda_checked: bool,
}

#[derive(Serialize)]
struct ClothingItem {
    id: String,
    name: String,
    brand: Option<String>,
    macro_category: String,
    micro_category: String,
    formality: String,
    color_hex: String,
    size: Option<String>,
    material: Option<String>,
    price: Option<f64>,
    image_url: String,
    notes: Option<String>,

    // Wardrobe Color Engine Metadata
    dominant_hex: Option<String>,
    vibrant_hex: Option<String>,
    muted_hex: Option<String>,
    palette_hexes: Vec<String>,
    color_temperature: Option<String>,
    seasonal_palette: Option<String>,
    contrast_level: Option<String>,
    ko_color_group: Option<String>,
}

fn main() {
    let cosmetics = vec![
        CosmeticItem {
            id: "lip_round_trip_001".to_string(),
            name: "Glow Catalyst Lip Stain".to_string(),
            brand: "KoColor Atelier".to_string(),
            macro_category: "Lips".to_string(),
            micro_category: "Lipstick".to_string(),
            formulation: "Liquid".to_string(),
            chemistry_base: "Water".to_string(),
            finish: "Radiant".to_string(),
            coverage: "Buildable".to_string(),
            temperature: "Cool".to_string(),
            color_hex: "#800020".to_string(),
            shade_name: Some("Velvet Mulberry".to_string()),
            image_url: "https://cdn.kocolor.com/inventory/assets/velvet_mulberry.webp".to_string(),
            notes: Some("A high-tech hybrid lip stain that provides weightless color and deep hydration.".to_string()),
            instructions: Some("Apply one coat for a blurred look. Layer for intense saturation.".to_string()),
            batch_code: Some("KC-LIP-2026-ST1".to_string()),
            pao_months: Some(12),
            price: Some(34.0),
            volume: Some("5ml".to_string()),
            expiry_date: Some(1798761600000), // 2027
            hero_ingredient: Some("Hyaluronic Acid".to_string()),
            skin_compatibility: Some("All skin types".to_string()),
            contains_fragrance: Some(false),
            ingredients: vec![
                "Water".to_string(),
                "Glycerin".to_string(),
                "Red 7 Lake".to_string(),
                "Hyaluronic Acid".to_string(),
                "Tocopherol".to_string(),
            ],
            allergens: vec![],
            eco_score: Some("A".to_string()),
            is_vegan: Some(true),
            is_cruelty_free: Some(true),
            recycling_instructions: Some("Packaging is 100% recyclable. Return to store for points.".to_string()),
            ritual_placement: Some("Morning Routine (Step 8)".to_string()),
            fda_recall_status: Some("Clear".to_string()),
            fda_adverse_event_count: 0,
            fda_clinical_warnings: vec!["For external use only.".to_string()],
            fda_top_reactions: vec![],
            fda_active_ingredients: vec![],
            is_fda_checked: true,
        },
    ];

    let response = StarterPackResponse {
        version: 1,
        cosmetics,
        clothing: vec![],
    };

    let json_payload = serde_json::to_string_pretty(&response).expect("Failed to serialize");

    let mut file = File::create("starter-pack.json").expect("Failed to create file");
    file.write_all(json_payload.as_bytes()).expect("Failed to write JSON");

    println!("✅ Successfully generated round-trip lipstick starter-pack.json!");
}
