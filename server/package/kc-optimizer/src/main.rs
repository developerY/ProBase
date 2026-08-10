mod models;
mod indexer;
mod optimizer;
mod composer;

use rayon::prelude::*;
use std::fs;
use std::path::Path;
use std::time::Instant;

fn main() {
    println!("🚀 Starting KoColor Asset Engineering Pipeline (v1.0)...");
    let start_time = Instant::now();

    let raw_assets_dir = "./raw_assets";
    let output_dist_dir = Path::new("./dist/assets");

    // Ensure the output directory exists
    fs::create_dir_all(output_dist_dir).expect("Failed to create dist directory");

    // --- PASS 1: DATA INGESTION ---
    println!("\n--- PASS 1: DATA INGESTION ---");
    let canonical_index = indexer::build_canonical_index(raw_assets_dir);

    // --- PASS 2: ASSET OPTIMIZATION ---
    println!("\n--- PASS 2: CONCURRENT ASSET OPTIMIZATION ---");

    // We convert the HashMap values to a parallel iterator to process all images concurrently
    canonical_index.par_iter().for_each(|(id, kpss_data)| {
        println!("⚙️  Optimizing assets for: {}...", id);

        // Pass the relative image path directly from the JSON payload
        let _optimized_assets = optimizer::process_asset_stream(
            &kpss_data.raw_image_input,
            output_dist_dir
        );

        // Note: In Task 4, we will use these returned OptimizedAssets to build the final KCPS Payload.
    });

    println!("\n✅ CCT Build completed in {:.2?}.", start_time.elapsed());
}
