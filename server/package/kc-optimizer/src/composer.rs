use crate::models::{KcpsPayload, KpssSource, PackageManifest};
use std::collections::HashMap;
use std::fs;
use std::path::Path;
use ed25519_dalek::SigningKey;
use serde::Serialize;
use crate::packager;

#[derive(Serialize)]
pub struct PackageManifestRecord {
    pub package_id: String,
    pub hash: String,
    pub signature: String,
    pub uncompressed_size_bytes: u64,
}

/// Parses TOML manifests, resolves product IDs from the canonical index,
/// and assembles the final KCPS wire payloads for distribution.
pub fn assemble_packages(
    canonical_index: &HashMap<String, KpssSource>,
    optimized_asset_data: &HashMap<String, crate::optimizer::OptimizedAssets>,
    config_dir: &str,
    dist_dir: &Path,
    signing_key: &SigningKey,
) -> Vec<PackageManifestRecord> {
    println!("📦 Scanning '{}' for TOML package manifests...", config_dir);

    let mut manifest_records = Vec::new();
    let config_path = Path::new(config_dir);
    if !config_path.exists() {
        println!("⚠️  Config directory not found. Skipping composition.");
        return manifest_records;
    }

    // Traverse the package_configs directory for .toml files
    for entry in fs::read_dir(config_path).expect("Failed to read config directory").flatten() {
        let path = entry.path();

        if path.is_file() && path.extension().and_then(|s| s.to_str()) == Some("toml") {
            println!("  📄 Processing manifest: {:?}", path.file_name().unwrap());

            let toml_content = fs::read_to_string(&path)
                .unwrap_or_else(|err| panic!("❌ Failed to read TOML {:?}: {}", path, err));

            let manifest: PackageManifest = toml::from_str(&toml_content)
                .unwrap_or_else(|err| panic!("❌ Invalid TOML structure in {:?}: {}", path, err));

            let mut package_payloads: Vec<KcpsPayload> = Vec::new();

            // Resolve each requested ID against the Canonical Index
            for product_id in &manifest.assortment.includes {
                let source_data = canonical_index.get(product_id).unwrap_or_else(|| {
                    panic!("❌ Composition Error: Product ID '{}' requested by package '{}' does not exist in the Canonical Index.", product_id, manifest.package_metadata.id);
                });

                let asset_data = optimized_asset_data.get(product_id).unwrap_or_else(|| {
                    panic!("❌ Composition Error: Missing optimized assets for '{}'.", product_id);
                });

                // Trigger compile-time science calculations
                let enriched = crate::enrichment::enrich_product(
                    &source_data.color_hex,
                    &source_data.ingredients,
                    source_data.contains_fragrance,
                    &source_data.name,
                    &source_data.brand,
                    &source_data.macro_category,
                );

                // Transform KPSS (Authoring) -> KCPS (Wire Object)
                // We map the fields and INJECT the generated CDN paths and BlurHashes.
                let kcps_item = KcpsPayload {
                    schema_version: 1,
                    id: source_data.id.clone(),
                    name: source_data.name.clone(),
                    brand: source_data.brand.clone(),
                    macro_category: source_data.macro_category.clone(),
                    micro_category: source_data.micro_category.clone(),
                    shade_name: source_data.shade_name.clone(),
                    color_hex: source_data.color_hex.clone(),

                    // Injected CCT Artifacts
                    blurhash: asset_data.blurhash.clone(),
                    image_url: format!("https://cdn.kocolor.com/assets/{}", asset_data.hero_filename),
                    thumbnail_url: format!("https://cdn.kocolor.com/assets/{}", asset_data.thumb_filename),

                    // Inject Scientific Enrichment
                    cielab: enriched.cielab,
                    safety_flags: enriched.safety_flags,

                    // Propagated Fields
                    notes: source_data.notes.clone(),
                    hero_ingredient: source_data.hero_ingredient.clone(),
                    price: source_data.price,
                    volume: source_data.volume.clone(),
                    eco_score: source_data.eco_score.clone(),
                    ingredients: source_data.ingredients.clone(),
                    contains_fragrance: source_data.contains_fragrance,
                    recycling_instructions: source_data.recycling_instructions.clone(),
                    fda_data_verified: source_data.fda_data_verified,
                };

                package_payloads.push(kcps_item);
            }

            // Serialize and hand off to the Packager for Zstd and Ed25519
            let final_json = serde_json::to_string(&package_payloads)
                .expect("❌ Failed to serialize KCPS payload");

            let (hash_hex, sig_hex, uncompressed_size) = packager::seal_package(
                final_json.as_bytes(),
                &manifest.package_metadata.id,
                signing_key,
                dist_dir
            );

            manifest_records.push(PackageManifestRecord {
                package_id: manifest.package_metadata.id.clone(),
                hash: hash_hex,
                signature: sig_hex,
                uncompressed_size_bytes: uncompressed_size,
            });

            println!("  🔒 Sealed package '{}' (.kpkg generated & signed).", manifest.package_metadata.id);
        }
    }

    manifest_records
}
