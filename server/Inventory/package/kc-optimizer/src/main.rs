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

fn main() {
    println!("🚀 Starting KoColor Asset Engineering Pipeline (v1.0)...");
    let start_time = Instant::now();

    // Secure Ed25519 keypair for the distribution pipeline
    // In production, this would be loaded from a secure ENV variable or vault.
    let private_key_hex = "dc1d99af3fb46499dfb365d75631746dff779aafa0646cddab2c15767c40735f";
    let private_key_bytes = hex::decode(private_key_hex).expect("❌ Invalid private key hex");
    let signing_key = SigningKey::from_bytes(&private_key_bytes.try_into().expect("❌ Invalid key length"));

    println!("🚀 Starting KoColor Asset Engineering Pipeline (v1.0)...");
    println!("🔑 Root of Trust Keypair:");
    println!("   Public Key (Hex):  {}", hex::encode(signing_key.verifying_key().to_bytes()));
    println!("   ⚠️  Update SecurityConstants.kt in Android with this Public Key for verification to pass.\n");

    let raw_assets_dir = "./input";
    let output_staging_dir = Path::new("./dist/staging/inventory/dist");
    let assets_dist_dir = output_staging_dir.join("assets");

    // Ensure the output directories exist
    fs::create_dir_all(&assets_dist_dir).expect("Failed to create staging directories");

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
        &output_staging_dir,
        &signing_key
    );

    // --- PASS 4: GENERATE MASTER MANIFEST ---
    println!("\n--- PASS 4: GENERATE MASTER MANIFEST ---");

    let manifest_data = models::PackManifestOutput {
        manifest_version: 1,
        generated_at: chrono::Utc::now().to_rfc3339(),
        compiler_version: env!("CARGO_PKG_VERSION").to_string(),
        key_id: "atelier-dev-01".to_string(),
        packs: manifest_records,
    };

    let manifest_data_json = serde_json::to_string(&manifest_data)
        .expect("❌ Failed to serialize manifest data");

    // ⚠️ CRITICAL: Root of Trust must be generated on the EXACT bytes written to the file.
    // We sign the compact version and write the file compactly to avoid canonicalization errors.
    let signature = packager::sign_data(manifest_data_json.as_bytes(), &signing_key);

    let envelope = models::SignedPayloadEnvelope {
        data: manifest_data,
        signature,
        package_version: "1.0.0".to_string(),
        schema_version: 1,
    };

    let manifest_json = serde_json::to_string(&envelope)
        .expect("❌ Failed to generate master manifest envelope");

    let manifest_path = output_staging_dir.join("manifest.json");
    fs::write(manifest_path, manifest_json).expect("❌ Failed to write manifest.json");
    println!("  📜 master manifest.json written (Compact Binary Trust established).");

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

    // Use to_string (compact) to match manifest trust style and Retrofit expectations
    let search_json = serde_json::to_string(&search_index)
        .expect("❌ Failed to generate search index");

    let search_path = output_staging_dir.join("search_index.json");
    fs::write(search_path, search_json).expect("❌ Failed to write search_index.json");
    println!("  🔍 search_index.json written (Compact Map format).");

    println!("\n✅ CCT Build completed in {:.2?}.", start_time.elapsed());
}
