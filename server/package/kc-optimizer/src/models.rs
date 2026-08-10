use serde::{Deserialize, Serialize};

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
