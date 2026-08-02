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
            id: "cosmetic_001".to_string(),
            name: "KoColor Signature Crimson Lip Color".to_string(),
            brand: "KoColor".to_string(),
            macro_category: "Lips".to_string(),
            micro_category: "Lipstick".to_string(),
            formulation: "Cream".to_string(),
            chemistry_base: "Oil".to_string(),
            finish: "Satin".to_string(),
            coverage: "Full".to_string(),
            temperature: "Neutral".to_string(),
            color_hex: "#5A1827".to_string(),
            shade_name: Some("Imperial Crimson".to_string()),
            image_url: "https://cdn.kocolor.com/inventory/assets/image_07157d.webp".to_string(),
            notes: Some("High-pigment professional lip color with 12-hour wear.".to_string()),
            instructions: Some("Apply to clean, dry lips. Use a lip brush for precision definition.".to_string()),
            batch_code: Some("KC-LIP-2026-X1".to_string()),
            pao_months: Some(18),
            price: Some(38.0),
            volume: Some("4g".to_string()),
            expiry_date: Some(1798761600000), // Jan 1, 2027
            hero_ingredient: Some("Camellia Japonica Seed Oil".to_string()),
            skin_compatibility: Some("All skin types".to_string()),
            contains_fragrance: Some(false),
            ingredients: vec![
                "Ricinus Communis Oil".to_string(),
                "Candelilla Cera".to_string(),
                "Camellia Japonica Seed Oil".to_string(),
                "Tocopherol".to_string(),
                "Iron Oxides".to_string(),
            ],
            allergens: vec![],
            eco_score: Some("A".to_string()),
            is_vegan: Some(true),
            is_cruelty_free: Some(true),
            recycling_instructions: Some("Tube is 100% PCR aluminum. Rinse and place in metal recycling.".to_string()),
            ritual_placement: Some("Morning Routine (Step 8)".to_string()),
            fda_recall_status: Some("Clear".to_string()),
            fda_adverse_event_count: 0,
            fda_clinical_warnings: vec!["Keep out of reach of children.".to_string()],
            fda_top_reactions: vec![],
            fda_active_ingredients: vec!["Titanium Dioxide".to_string()],
            is_fda_checked: true,
        },
        CosmeticItem {
            id: "cosmetic_002".to_string(),
            name: "KoColor Velvet Canvas Primer".to_string(),
            brand: "KoColor".to_string(),
            macro_category: "Skincare & Prep".to_string(),
            micro_category: "Primer".to_string(),
            formulation: "Gel".to_string(),
            chemistry_base: "Silicone".to_string(),
            finish: "Matte".to_string(),
            coverage: "Sheer".to_string(),
            temperature: "Neutral".to_string(),
            color_hex: "#FFFFFF".to_string(),
            shade_name: Some("Clear".to_string()),
            image_url: "https://cdn.kocolor.com/inventory/assets/image_velvet_primer.webp".to_string(),
            notes: Some("Pore-blurring primer for a glass-skin foundation base.".to_string()),
            instructions: Some("Apply a pea-sized amount after SPF and before foundation.".to_string()),
            batch_code: Some("KC-PRM-2026-B2".to_string()),
            pao_months: Some(12),
            price: Some(42.0),
            volume: Some("30ml".to_string()),
            expiry_date: Some(1798761600000),
            hero_ingredient: Some("Niacinamide".to_string()),
            skin_compatibility: Some("Oily to Combination".to_string()),
            contains_fragrance: Some(false),
            ingredients: vec!["Dimethicone".to_string(), "Niacinamide".to_string(), "Silica".to_string(), "Aqua".to_string()],
            allergens: vec![],
            eco_score: Some("B".to_string()),
            is_vegan: Some(true),
            is_cruelty_free: Some(true),
            recycling_instructions: Some("Glass bottle is recyclable. Pump should be disposed of separately.".to_string()),
            ritual_placement: Some("Morning Routine (Step 6)".to_string()),
            fda_recall_status: Some("Clear".to_string()),
            fda_adverse_event_count: 0,
            fda_clinical_warnings: vec![],
            fda_top_reactions: vec![],
            fda_active_ingredients: vec![],
            is_fda_checked: true,
        },
    ];

    let clothing = vec![
        ClothingItem {
            id: "clothing_001".to_string(),
            name: "Premium Silk Button-Up".to_string(),
            brand: Some("KoColor Atelier".to_string()),
            macro_category: "Tops".to_string(),
            micro_category: "Tops".to_string(),
            formality: "PROFESSIONAL".to_string(),
            color_hex: "#0F52BA".to_string(),
            size: Some("M".to_string()),
            material: Some("100% Mulberry Silk".to_string()),
            price: Some(120.0),
            image_url: "https://cdn.kocolor.com/inventory/assets/clothing_001.webp".to_string(),
            notes: Some("Essential professional anchor piece.".to_string()),
            dominant_hex: Some("#0F52BA".to_string()),
            vibrant_hex: Some("#1E90FF".to_string()),
            muted_hex: Some("#4682B4".to_string()),
            palette_hexes: vec!["#0F52BA".to_string(), "#FFFFFF".to_string()],
            color_temperature: Some("COOL".to_string()),
            seasonal_palette: Some("WINTER".to_string()),
            contrast_level: Some("HIGH".to_string()),
            ko_color_group: Some("Cool Winter".to_string()),
        }
    ];

    let response = StarterPackResponse {
        version: 1,
        cosmetics,
        clothing,
    };

    let json_payload = serde_json::to_string_pretty(&response).expect("Failed to serialize");

    let mut file = File::create("starter-pack.json").expect("Failed to create file");
    file.write_all(json_payload.as_bytes()).expect("Failed to write JSON");

    println!("✅ Successfully generated max-fidelity starter-pack.json v1!");
}
