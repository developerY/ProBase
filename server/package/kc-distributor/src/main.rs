use anyhow::Result;
use std::path::Path;

mod zip;
mod assets;

fn main() -> Result<()> {
    println!("========================================");
    println!(" KoColor Sovereign Distribution");
    println!("========================================");

    let input_dir = Path::new("input");
    let dist_dir = Path::new("dist");
    let deployment_dir = dist_dir.join("deployment");
    let output_zip = dist_dir.join("kocolor-v1-deploy.zip");

    // 1. Process Assets (WebP Transcoding & Flattening)
    println!("\n[1/2] Processing assets and metadata...");
    assets::process_and_flatten_assets(input_dir, dist_dir)?;

    // 2. Create Deployment ZIP
    println!("\n[2/2] Creating deployment archive...");
    println!("Source : {}", deployment_dir.display());
    println!("Output : {}", output_zip.display());
    println!();

    zip::create_deployment_zip(&deployment_dir, &output_zip)?;

    println!("\n✓ Deployment Ready");
    println!("  {}", output_zip.display());
    println!("========================================");

    Ok(())
}
