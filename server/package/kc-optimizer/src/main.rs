mod models;
mod indexer;
mod optimizer;
mod composer;

use std::time::Instant;

fn main() {
    println!("🚀 Starting KoColor Asset Engineering Pipeline (v1.0)...");
    let start_time = Instant::now();

    // Pass 1: Build the Canonical Product Index
    println!("--- PASS 1: DATA INGESTION ---");
    let canonical_index = indexer::build_canonical_index("./raw_assets");

    // Optional: Print a debug summary of the first item to verify it worked
    if let Some((id, data)) = canonical_index.iter().next() {
        println!("🛠️ Debug - First indexed item: {} -> {} ({})", id, data.name, data.brand);
    }

    // Pass 2: Process Asset Streams (Rayon parallel execution would be invoked here)
    // println!("⚙️  Computing image optimizations and BlurHashes...");
    // Iterate through index, run optimizer::process_asset_stream()

    // Pass 3: Compose Packages & Sign
    // println!("📝 Resolving TOML assortments & packaging...");
    // composer::build_packages(&canonical_index, "./package_configs");

    println!("✅ Build completed in {:.2?}.", start_time.elapsed());
}
