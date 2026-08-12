use anyhow::Result;
use image::imageops::FilterType;
use std::fs;
use std::path::Path;
use walkdir::WalkDir;

pub fn process_and_flatten_assets(input_dir: &Path, dist_dir: &Path) -> Result<()> {
    println!("🧹 Preparing distribution directories...");

    // The "Source of Truth" for the ZIP bundle
    let deployment_dir = dist_dir.join("deployment");

    // Create flattened structure inside deployment directory
    let hero_dir = deployment_dir.join("assets/hero");
    let thumb_dir = deployment_dir.join("assets/thumb");
    let notes_dir = deployment_dir.join("notes");
    let json_dir = deployment_dir.join("json");

    fs::create_dir_all(&hero_dir)?;
    fs::create_dir_all(&thumb_dir)?;
    fs::create_dir_all(&notes_dir)?;
    fs::create_dir_all(&json_dir)?;

    println!("🔍 Scanning input for images, notes, and JSON...");

    for entry in WalkDir::new(input_dir).into_iter().filter_map(|e| e.ok()) {
        let path = entry.path();
        if !path.is_file() {
            continue;
        }

        let file_name = path.file_name().unwrap().to_str().unwrap();
        let stem = path.file_stem().unwrap().to_str().unwrap();

        // Route: Notes JSON
        if file_name.ends_with(".notes.json") {
            let target = notes_dir.join(file_name);
            fs::copy(path, target)?;
            println!("  📝 Flattened Note: {}", file_name);
        }
        // Route: Core Product JSON
        else if file_name.ends_with(".json") && !file_name.ends_with(".notes.json") {
            let target = json_dir.join(file_name);
            fs::copy(path, target)?;
            println!("  📄 Flattened JSON: {}", file_name);
        }
        // Route: Images (Transcode to WebP)
        else if file_name.ends_with(".png") || file_name.ends_with(".jpg") {
            println!("  🖼️ Transcoding Image: {}", file_name);
            let img = image::open(path)?;

            // Generate Hero Image (1024x1024 Lanczos3)
            let hero_path = hero_dir.join(format!("{}.webp", stem));
            let hero_img = img.resize_to_fill(1024, 1024, FilterType::Lanczos3);
            hero_img.save(&hero_path)?;

            // Generate Thumb Image (256x256 Triangle)
            let thumb_path = thumb_dir.join(format!("{}_thumb.webp", stem));
            let thumb_img = img.resize_to_fill(256, 256, FilterType::Triangle);
            thumb_img.save(&thumb_path)?;
        }
    }

    Ok(())
}
