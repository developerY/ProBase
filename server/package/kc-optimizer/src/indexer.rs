use crate::models::KpssSource;
use std::collections::HashMap;
use std::fs;
use walkdir::WalkDir;

/// Traverses raw_assets/, parses JSON, and validates semantic identity.
/// Returns the in-memory Canonical Product Index.
pub fn build_canonical_index(raw_assets_dir: &str) -> HashMap<String, KpssSource> {
    let mut index = HashMap::new();

    println!("🔍 Scanning '{}' for KPSS v1 authoring files...", raw_assets_dir);

    for entry in WalkDir::new(raw_assets_dir).into_iter().filter_map(|e| e.ok()) {
        let path = entry.path();

        // We only care about parsing the JSON authoring files here.
        if path.is_file() && path.extension().and_then(|s| s.to_str()) == Some("json") {
            let file_content = fs::read_to_string(path)
                .unwrap_or_else(|err| panic!("❌ Failed to read file {:?}: {}", path, err));

            // Deserialize against our strict KPSS v1 struct
            let mut kpss_source: KpssSource = serde_json::from_str(&file_content)
                .unwrap_or_else(|err| panic!("❌ Invalid KPSS v1 JSON in {:?}: {}", path, err));

            // Resolve relative image path to absolute/full path from root
            if kpss_source.raw_image_input.starts_with("./") {
                let parent = path.parent().expect("Failed to get parent dir");
                let resolved = parent.join(&kpss_source.raw_image_input[2..]);
                kpss_source.raw_image_input = resolved.to_string_lossy().to_string();
            }

            // Architecture Safeguard: Enforce Schema Version
            if kpss_source.schema_version != 1 {
                panic!("❌ Unsupported schema version in {:?}. Expected 1, found {}", path, kpss_source.schema_version);
            }

            let id = kpss_source.id.clone();

            // Prevent duplicate IDs across the entire catalog
            if index.contains_key(&id) {
                panic!("❌ Duplicate Product ID found: {}. IDs must be unique.", id);
            }

            println!("  ✅ Indexed: {} ({})", id, kpss_source.name);
            index.insert(id, kpss_source);
        }
    }

    println!("📦 Canonical Product Index built with {} items.\n", index.len());
    index
}
