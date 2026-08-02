use serde::{Deserialize, Serialize};

pub mod inventory;

#[derive(Debug, Serialize, Deserialize)]
pub struct StarterPackResponse {
    pub version: u32,
    pub cosmetics: Vec<CosmeticItem>,
    pub clothing: Vec<ClothingItem>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct CosmeticItem {
    pub id: String,
    pub name: String,
    pub brand: String,
    pub macro_category: String,
    pub micro_category: String,

    // Level 3 Professional Facets
    pub formulation: String,
    pub chemistry_base: String,
    pub finish: String,
    pub coverage: String,
    pub temperature: String,

    // UI Visuals
    pub color_hex: String,
    pub shade_name: Option<String>,
    pub image_url: String,
    pub notes: Option<String>,
    pub instructions: Option<String>,

    // Professional Inventory & Logistics
    pub batch_code: Option<String>,
    pub pao_months: Option<u32>,
    pub price: Option<f64>,
    pub volume: Option<String>,
    pub expiry_date: Option<u64>,

    // Algorithmic & AI Insights
    pub hero_ingredient: Option<String>,
    pub skin_compatibility: Option<String>,
    pub contains_fragrance: Option<bool>,
    pub ingredients: Vec<String>,
    pub allergens: Vec<String>,

    // Sustainability & Eco-Impact
    pub eco_score: Option<String>,
    pub is_vegan: Option<bool>,
    pub is_cruelty_free: Option<bool>,
    pub recycling_instructions: Option<String>,

    // Ritual Context
    pub ritual_placement: Option<String>,

    // FDA & Clinical Safety
    pub fda_recall_status: Option<String>,
    pub fda_adverse_event_count: u32,
    pub fda_clinical_warnings: Vec<String>,
    pub fda_top_reactions: Vec<String>,
    pub fda_active_ingredients: Vec<String>,
    pub is_fda_checked: bool,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ClothingItem {
    pub id: String,
    pub name: String,
    pub brand: Option<String>,
    pub macro_category: String,
    pub micro_category: String,
    pub formality: String,
    pub color_hex: String,
    pub size: Option<String>,
    pub material: Option<String>,
    pub price: Option<f64>,
    pub image_url: String,
    pub notes: Option<String>,

    // Wardrobe Color Engine Metadata
    pub dominant_hex: Option<String>,
    pub vibrant_hex: Option<String>,
    pub muted_hex: Option<String>,
    pub palette_hexes: Vec<String>,
    pub color_temperature: Option<String>,
    pub seasonal_palette: Option<String>,
    pub contrast_level: Option<String>,
    pub ko_color_group: Option<String>,
}
