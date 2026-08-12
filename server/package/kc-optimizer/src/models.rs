use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Clone)]
pub struct KcpsPackagePayload {
    pub schema_version: u8,
    pub cosmetics: Vec<CosmeticItemDto>,
    pub clothing: Vec<ClothingItemDto>,
}

#[derive(Debug, Serialize, Clone)]
pub struct CosmeticItemDto {
    pub id: String,
    pub name: String,
    pub brand: String,
    pub macro_category: String,
    pub micro_category: String,
    pub color_hex: String,
    pub shade_name: Option<String>,
    pub image_url: String,
    pub thumbnail_url: String,
    pub price: Option<f32>,
    pub notes: Option<String>,
    pub formulation: Option<String>,
    pub chemistry_base: Option<String>,
    pub finish: Option<String>,
    pub coverage: Option<String>,
    pub temperature: Option<String>,
    pub volume: Option<String>,
    pub pao_months: Option<u32>,
    pub expiry_date: Option<u64>,
    pub instructions: Option<String>,
    pub ingredients: Vec<String>,
    pub allergens: Vec<String>,
    pub is_vegan: Option<bool>,
    pub is_cruelty_free: Option<bool>,
    pub fda_data_verified: bool,

    // Enrichment
    pub calculated_chemistry_phase: Option<String>,
    pub cielab: Option<Vec<f32>>,
    pub blurhash: Option<String>,
    pub calculated_safety_flags: Option<SafetyFlags>,
    pub calculated_hero_actives: Vec<String>,
    pub calculated_unit_price: Option<f32>,
    pub calculated_search_tokens: Vec<String>,
}

#[derive(Debug, Serialize, Clone)]
pub struct SafetyFlags {
    pub is_silicone_free: bool,
    pub is_paraben_free: bool,
    pub is_sulfate_free: bool,
}

#[derive(Debug, Serialize, Clone)]
pub struct ClothingItemDto {
    pub id: String,
    pub name: String,
    pub brand: String,
    pub macro_category: String,
    pub micro_category: String,
    pub color_hex: String,
    pub shade_name: Option<String>,
    pub image_url: String,
    pub thumbnail_url: String,
    pub price: Option<f32>,
    pub notes: Option<String>,
    pub formality: Option<String>,
    pub material: Option<String>,
    pub dominant_hex: Option<String>,
    pub vibrant_hex: Option<String>,
    pub muted_hex: Option<String>,
    pub palette_hexes: Vec<String>,
    pub color_temperature: Option<String>,
    pub seasonal_palette: Option<String>,
    pub contrast_level: Option<String>,
    pub ko_color_group: Option<String>,

    // Enrichment
    pub blurhash: Option<String>,
    pub calculated_unit_price: Option<f32>,
    pub calculated_search_tokens: Vec<String>,
}

/// KPSS v1: The Authoring Source Contract
/// This structure maps exactly to what authors write in the raw JSON files.
#[derive(Debug, Deserialize, Clone)]
pub struct KpssSource {
    pub schema_version: u8,
    pub id: String,
    pub name: String,
    pub brand: String,
    pub macro_category: String,
    pub micro_category: String,
    pub shade_name: String,
    pub color_hex: String,

    // Relative path to the raw high-res PNG asset
    pub raw_image_input: String,

    // Additional authoring fields required for the final payload
    pub notes: Option<String>,
    pub hero_ingredient: Option<String>,
    pub price: f32,
    pub volume: String,
    pub eco_score: Option<String>,
    pub ingredients: Vec<String>,

    // Handles variations in authoring keys, mapping them cleanly
    #[serde(alias = "Contains_Fragrance")]
    pub contains_fragrance: bool,
    pub recycling_instructions: Option<String>,
    pub fda_data_verified: bool,
}

/// KCPS v1: The Optimized Wire Contract
/// This is the final object that gets serialized into the .kpkg binary.
#[derive(Debug, Serialize, Clone)]
pub struct KcpsPayload {
    pub schema_version: u8,
    pub id: String,
    pub name: String,
    pub brand: String,
    pub macro_category: String,
    pub micro_category: String,
    pub shade_name: String,
    pub color_hex: String,

    // Generated CCT Artifacts (Injected by the compiler)
    pub blurhash: String,
    pub image_url: String,
    pub thumbnail_url: String,

    // Advanced Scientific Enrichment (Computed)
    pub cielab: [f32; 3],
    pub safety_flags: Vec<String>,

    // Propagated Fields
    pub notes: Option<String>,
    pub hero_ingredient: Option<String>,
    pub price: f32,
    pub volume: String,
    pub eco_score: Option<String>,
    pub ingredients: Vec<String>,
    pub contains_fragrance: bool,
    pub recycling_instructions: Option<String>,
    pub fda_data_verified: bool,
}

#[derive(Debug, Serialize, Clone)]
pub struct PackInfo {
    pub id: String,
    pub name: String,
    pub description: String,
    pub version: u32,
    pub publisher: String,
    #[serde(rename = "type")]
    pub pack_type: String,
    pub endpoint: String,
    pub item_count: usize,
    pub compressed_size_bytes: u64,
    pub uncompressed_size_bytes: u64,
    pub sha256: String,
    pub signature: String,
    pub compression_algorithm: String,
    pub hash_algorithm: String,
    pub hash_encoding: String,
    pub signature_algorithm: String,
    pub signature_encoding: String,
    pub package_format_version: u32,
    pub schema_version: u32,
    pub encryption: String,
    pub hero_image_url: Option<String>,
    pub expires_at: Option<u64>,
    pub preview_items: Vec<PreviewItem>,
}

#[derive(Debug, Serialize, Clone)]
pub struct PreviewItem {
    pub name: String,
    pub description: String,
}

#[derive(Debug, Serialize, Clone)]
pub struct PackManifestOutput {
    pub manifest_version: u32,
    pub generated_at: String,
    pub compiler_version: String,
    pub key_id: String,
    pub packs: Vec<PackInfo>,
}

#[derive(Debug, Serialize, Clone)]
pub struct SignedPayloadEnvelope<T> {
    pub data: T,
    pub signature: String,
    pub package_version: String,
    pub schema_version: u32,
}

/// TOML Package Manifest
#[derive(Debug, Deserialize)]
pub struct PackageManifest {
    pub package_metadata: PackageMetadata,
    pub assortment: Assortment,
}

#[derive(Debug, Deserialize)]
pub struct PackageMetadata {
    pub id: String,
    pub name: String,
    pub description: String,
}

#[derive(Debug, Deserialize)]
pub struct Assortment {
    pub includes: Vec<String>,
}
