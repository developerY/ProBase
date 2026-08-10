use serde::{Deserialize, Serialize};

/// KPSS v1: The Authoring Source Contract
/// This is minimal, zero-noise, and contains no generated artifacts.
#[derive(Debug, Deserialize, Clone)]
pub struct KpssSource {
    pub schema_version: u8,
    pub id: String,
    pub brand: String,
    pub macro_category: String,
    pub micro_category: String,
    pub shade_name: String,
    pub color_hex: String,
    pub raw_image_input: String,
}

/// KCPS v1: The Optimized Wire Contract
/// Intermediate data is purged. Includes generated BlurHash and WebP paths.
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

    // Generated CCT Artifacts
    pub blurhash: String,
    pub image_url: String,
    pub thumbnail_url: String,

    // Other contract fields would follow here...
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
