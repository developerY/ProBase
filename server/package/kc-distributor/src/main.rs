use image::imageops::FilterType;
use std::fs::{self, File};
use std::io::{Read, Write};
use std::path::Path;
use walkdir::WalkDir;
use zip::write::FileOptions;

fn main() {
    // Assuming the script is run from a root where input and dist (from optimizer) exist
    let raw_dir = Path::new("input");
    let dist_dir = Path::new("dist");

    println!("🧹 Preparing distribution directories...");
    // We don't remove dist_dir entirely because it might contain manifest.json and .kpkg from the optimizer
    // But we ensure the assets/notes/json subdirectories exist and are clean for flattening
    fs::create_dir_all(dist_dir.join("assets/hero")).unwrap();
    fs::create_dir_all(dist_dir.join("assets/thumb")).unwrap();
    fs::create_dir_all(dist_dir.join("notes")).unwrap();
    fs::create_dir_all(dist_dir.join("json")).unwrap();

    println!("🔍 Scanning input for images, notes, and JSON...");

    for entry in WalkDir::new(raw_dir).into_iter().filter_map(|e| e.ok()) {
        let path = entry.path();
        if !path.is_file() {
            continue;
        }

        let file_name = path.file_name().unwrap().to_str().unwrap();
        let stem = path.file_stem().unwrap().to_str().unwrap();

        // Route: Notes JSON
        if file_name.ends_with(".notes.json") {
            let target = dist_dir.join("notes").join(file_name);
            fs::copy(path, target).unwrap();
            println!("📝 Flattened Note: {}", file_name);
        }
        // Route: Core Product JSON (Avoid copying .notes.json as .json)
        else if file_name.ends_with(".json") && !file_name.ends_with(".notes.json") {
            let target = dist_dir.join("json").join(file_name);
            fs::copy(path, target).unwrap();
            println!("📄 Flattened JSON: {}", file_name);
        }
        // Route: Images (Step 3: Transcode to WebP)
        else if file_name.ends_with(".png") || file_name.ends_with(".jpg") {
            println!("🖼️ Transcoding Image: {}", file_name);
            let img = image::open(path).expect("Failed to open image");

            // Define the base ID (stripping the .png/.jpg extension)
            let base_id = stem;

            // Generate Hero Image (1024x1024 Lanczos3)
            let hero_path = dist_dir.join("assets/hero").join(format!("{}.webp", base_id));
            let hero_img = img.resize_to_fill(1024, 1024, FilterType::Lanczos3);
            hero_img.save(&hero_path).expect("Failed to save hero webp");

            // Generate Thumb Image (256x256 Triangle for speed)
            let thumb_path = dist_dir.join("assets/thumb").join(format!("{}_thumb.webp", base_id));
            let thumb_img = img.resize_to_fill(256, 256, FilterType::Triangle);
            thumb_img.save(&thumb_path).expect("Failed to save thumb webp");
        }
    }

    // Step 4: The ZIP Engine
    let zip_path = dist_dir.join("kocolor-v1-deploy.zip");
    println!("🗜️ Zipping distribution payload to {:?}...", zip_path);
    create_zip(dist_dir, &zip_path);

    println!("🚀 Distribution packaging complete! Immutable artifact ready for CDN.");
}

fn create_zip(source_dir: &Path, output_zip: &Path) {
    let file = File::create(output_zip).expect("Failed to create zip file");
    let mut zip = zip::ZipWriter::new(file);
    let options = FileOptions::default()
        .compression_method(zip::CompressionMethod::Deflated)
        .unix_permissions(0o755);

    for entry in WalkDir::new(source_dir).into_iter().filter_map(|e| e.ok()) {
        let path = entry.path();
        let name = path.strip_prefix(source_dir).unwrap().to_str().unwrap();

        if path.is_file() {
            zip.start_file(name, options).unwrap();
            let mut f = File::open(path).unwrap();
            let mut buffer = Vec::new();
            f.read_to_end(&mut buffer).unwrap();
            zip.write_all(&buffer).unwrap();
        } else if !name.is_empty() {
            zip.add_directory(name, options).unwrap();
        }
    }
    zip.finish().unwrap();
}
