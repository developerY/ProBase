mod models;
mod indexer;
mod optimizer;
mod composer;
mod packager;
mod enrichment;

use rayon::prelude::*;
use std::fs;
use std::path::Path;
use std::time::Instant;
use ed25519_dalek::SigningKey;
use rand::rngs::OsRng;

fn main() {
    println!("🚀 Starting KoColor Asset Engineering Pipeline (v1.0)...");
    let start_time = Instant::now();

    // Generate a secure Ed25519 keypair for this build run
    // (In production, this would be loaded from a secure ENV variable or vault)
    let mut csprng = OsRng;
    let signing_key = SigningKey::generate(&mut csprng);

    let raw_assets_dir = "./input";
    let output_dist_dir = Path::new("./dist/deployment");
    let assets_dist_dir = output_dist_dir.join("assets");

    // Ensure the output directories exist
    fs::create_dir_all(&assets_dist_dir).expect("Failed to create dist/assets directory");

    // --- PASS 1: DATA INGESTION ---
    println!("\n--- PASS 1: DATA INGESTION ---");
    let canonical_index = indexer::build_canonical_index(raw_assets_dir);

    // --- PASS 2: CONCURRENT ASSET OPTIMIZATION ---
    println!("\n--- PASS 2: CONCURRENT ASSET OPTIMIZATION ---");

    // We convert the HashMap values to a parallel iterator to process all images concurrently
    // Collect the results into a HashMap for the Composer
    let optimized_asset_map: std::collections::HashMap<String, optimizer::OptimizedAssets> = canonical_index
        .par_iter()
        .map(|(id, kpss_data)| {
            println!("⚙️  Optimizing assets for: {}...", id);
            let assets = optimizer::process_asset_stream(&kpss_data.raw_image_input, &assets_dist_dir);
            (id.clone(), assets)
        })
        .collect();

    // --- PASS 3: COMPOSITION & PACKAGING ---
    println!("\n--- PASS 3: COMPOSITION & PACKAGING ---");
    let manifest_records = composer::assemble_packages(
        &canonical_index,
        &optimized_asset_map,
        "./input/package_configs",
        output_dist_dir,
        &signing_key
    );

    // --- PASS 4: GENERATE MASTER MANIFEST ---
    println!("\n--- PASS 4: GENERATE MASTER MANIFEST ---");
    let manifest_json = serde_json::to_string_pretty(&manifest_records)
        .expect("❌ Failed to generate master manifest");

    let manifest_path = output_dist_dir.join("manifest.json");
    fs::write(manifest_path, manifest_json).expect("❌ Failed to write manifest.json");
    println!("  📜 master manifest.json written.");

    // --- PASS 5: GENERATE GLOBAL SEARCH INDEX ---
    println!("\n--- PASS 5: GENERATE SEARCH INDEX ---");

    // Build a lightweight mapping of ID -> Search Tokens for the mobile Hub
    let mut search_index = std::collections::HashMap::new();

    for (id, data) in &canonical_index {
        let tokens = enrichment::generate_search_tokens(
            &data.name,
            &data.brand,
            &data.macro_category
        );
        search_index.insert(id.clone(), tokens);
    }

    let search_json = serde_json::to_string_pretty(&search_index)
        .expect("❌ Failed to generate search index");

    let search_path = output_dist_dir.join("search_index.json");
    fs::write(search_path, search_json).expect("❌ Failed to write search_index.json");
    println!("  🔍 search_index.json written.");

    println!("\n✅ CCT Build completed in {:.2?}.", start_time.elapsed());
}
