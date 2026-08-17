use anyhow::Result;
use std::path::Path;

mod zip;
mod assets;

fn main() -> Result<()> {
    println!("========================================");
    println!(" KoColor Sovereign Distribution");
    println!("========================================");

    let input_dir = Path::new("input");
    let dist_root = Path::new("dist");
    let staging_dir = dist_root.join("staging");
    let inventory_dir = staging_dir.join("inventory");
    let deployment_dir = inventory_dir.join("dist");
    let output_zip = dist_root.join("kocolor-v1-deploy.zip");

    // 1. Process Assets (WebP Transcoding & Flattening)
    println!("\n[1/2] Processing assets and metadata...");
    assets::process_and_flatten_assets(input_dir, &inventory_dir)?;

    // 2. Create info.txt with release metadata
    println!("\n[1.5/2] Generating info.txt...");
    let info_path = deployment_dir.join("info.txt");
    let info_content = format!(
        "KoColor Sovereign Distribution\nVersion: 1.0.0\nRelease Date: {}\nPlatform: V1-STABLE\n",
        chrono::Utc::now().to_rfc3339()
    );
    std::fs::write(info_path, info_content)?;

    // 3. Create Deployment ZIP
    println!("\n[2/2] Creating deployment archive...");
    println!("Source : {}", staging_dir.display());
    println!("Output : {}", output_zip.display());
    println!();

    zip::create_deployment_zip(&staging_dir, &output_zip)?;

    println!("\n✓ Deployment Ready");
    println!("  {}", output_zip.display());
    println!("========================================");

    Ok(())
}
