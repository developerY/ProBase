use crate::models::{KcpsPackagePayload, KpssSource, PackageManifest, PackInfo, PreviewItem, CosmeticItemDto, ClothingItemDto};
use std::collections::HashMap;
use std::fs;
use std::path::Path;
use ed25519_dalek::SigningKey;
use crate::packager;

/// Parses TOML manifests, resolves product IDs from the canonical index,
/// and assembles the final KCPS wire payloads for distribution.
pub fn assemble_packages(
    canonical_index: &HashMap<String, KpssSource>,
    optimized_asset_data: &HashMap<String, crate::optimizer::OptimizedAssets>,
    config_dir: &str,
    dist_dir: &Path,
    signing_key: &SigningKey,
) -> Vec<PackInfo> {
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

            let mut cosmetic_payloads: Vec<CosmeticItemDto> = Vec::new();
            let mut clothing_payloads: Vec<ClothingItemDto> = Vec::new();

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

                // Heuristic: If it has ingredients and is in a beauty category, it's a cosmetic
                if source_data.macro_category != "TOPS" && source_data.macro_category != "BOTTOMS" {
                    let kcps_item = CosmeticItemDto {
                        id: source_data.id.clone(),
                        name: source_data.name.clone(),
                        brand: source_data.brand.clone(),
                        macro_category: source_data.macro_category.clone(),
                        micro_category: source_data.micro_category.clone(),
                        shade_name: Some(source_data.shade_name.clone()),
                        color_hex: source_data.color_hex.clone(),

                        // Injected CCT Artifacts
                        blurhash: Some(asset_data.blurhash.clone()),
                        image_url: format!("https://cdn.kocolor.com/inventory/dist/assets/hero/{}", asset_data.hero_filename),
                        thumbnail_url: format!("https://cdn.kocolor.com/inventory/dist/assets/thumb/{}", asset_data.thumb_filename),

                        // Inject Scientific Enrichment
                        cielab: Some(enriched.cielab.to_vec()),
                        calculated_safety_flags: Some(enriched.safety_flags),
                        calculated_search_tokens: enriched.search_tokens,

                        // Propagated Fields
                        notes: source_data.notes.clone(),
                        price: Some(source_data.price),
                        volume: Some(source_data.volume.clone()),
                        ingredients: source_data.ingredients.clone(),
                        fda_data_verified: source_data.fda_data_verified,

                        // Defaults for optional fields
                        formulation: None,
                        chemistry_base: None,
                        finish: None,
                        coverage: None,
                        temperature: None,
                        pao_months: None,
                        expiry_date: None,
                        instructions: source_data.recycling_instructions.clone(),
                        allergens: Vec::new(),
                        is_vegan: None,
                        is_cruelty_free: None,
                        calculated_chemistry_phase: None,
                        calculated_hero_actives: source_data.hero_ingredient.clone().map(|h| vec![h]).unwrap_or_default(),
                        calculated_unit_price: Some(source_data.price),
                    };
                    cosmetic_payloads.push(kcps_item);
                } else {
                    let kcps_item = ClothingItemDto {
                        id: source_data.id.clone(),
                        name: source_data.name.clone(),
                        brand: source_data.brand.clone(),
                        macro_category: source_data.macro_category.clone(),
                        micro_category: source_data.micro_category.clone(),
                        color_hex: source_data.color_hex.clone(),
                        shade_name: Some(source_data.shade_name.clone()),
                        image_url: format!("https://cdn.kocolor.com/inventory/dist/assets/hero/{}", asset_data.hero_filename),
                        thumbnail_url: format!("https://cdn.kocolor.com/inventory/dist/assets/thumb/{}", asset_data.thumb_filename),
                        price: Some(source_data.price),
                        notes: source_data.notes.clone(),

                        // Injected CCT Artifacts
                        blurhash: Some(asset_data.blurhash.clone()),
                        calculated_unit_price: Some(source_data.price),
                        calculated_search_tokens: enriched.search_tokens,

                        // Defaults
                        formality: None,
                        material: None,
                        dominant_hex: None,
                        vibrant_hex: None,
                        muted_hex: None,
                        palette_hexes: Vec::new(),
                        color_temperature: None,
                        seasonal_palette: None,
                        contrast_level: None,
                        ko_color_group: None,
                    };
                    clothing_payloads.push(kcps_item);
                }
            }

            let package_payload = KcpsPackagePayload {
                schema_version: 1,
                cosmetics: cosmetic_payloads,
                clothing: clothing_payloads,
            };

            // Serialize and hand off to the Packager for Zstd and Ed25519
            let final_json = serde_json::to_string(&package_payload)
                .expect("❌ Failed to serialize KCPS payload");

            // Measure the byte length BEFORE compression
            let uncompressed_bytes = final_json.as_bytes();
            let size_bytes = uncompressed_bytes.len();

            let (hash_hex, sig_hex, uncompressed_size, compressed_size) = packager::seal_package(
                uncompressed_bytes,
                &manifest.package_metadata.id,
                signing_key,
                dist_dir
            );

            // Build Preview Items (First 3 products)
            let mut preview_items = Vec::new();
            for p in package_payload.cosmetics.iter().take(3) {
                preview_items.push(PreviewItem {
                    name: p.name.clone(),
                    description: p.notes.clone().unwrap_or_default(),
                });
            }

            // Select Hero Image (Using the first item's THUMBNAIL for faster loading in the Hub)
            let hero_image_url = package_payload.cosmetics.first()
                .map(|p| p.thumbnail_url.clone())
                .or_else(|| package_payload.clothing.first().map(|p| p.thumbnail_url.clone()));

            manifest_records.push(PackInfo {
                id: manifest.package_metadata.id.clone(),
                name: manifest.package_metadata.name.clone(),
                description: manifest.package_metadata.description.clone(),
                version: 1,
                publisher: "Atelier Engineering".to_string(),
                pack_type: "CORE".to_string(),
                endpoint: format!("{}.kpkg", manifest.package_metadata.id),
                item_count: package_payload.cosmetics.len() + package_payload.clothing.len(),
                compressed_size_bytes: compressed_size,
                uncompressed_size_bytes: uncompressed_size,
                sha256: hash_hex,
                signature: sig_hex,
                compression_algorithm: "zstd".to_string(),
                hash_algorithm: "sha256".to_string(),
                hash_encoding: "hex".to_string(),
                signature_algorithm: "ed25519".to_string(),
                signature_encoding: "hex".to_string(),
                package_format_version: 1,
                schema_version: 1,
                encryption: "none".to_string(),
                hero_image_url,
                expires_at: None,
                preview_items,
            });

            println!("  🔒 Sealed package '{}' (Size: {} bytes).", manifest.package_metadata.id, size_bytes);
        }
    }

    manifest_records
}
