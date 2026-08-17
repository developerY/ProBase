use image::{GenericImageView, ImageFormat};
use std::fs;
use std::path::Path;

fn main() {
    let in_dir = "input_images";
    let out_dir = "output_slices";

    // 1. Prepare directories
    if !Path::new(in_dir).exists() {
        fs::create_dir_all(in_dir).expect("❌ Failed to create input directory");
        println!("📁 Created '{}' directory. Please drop your grids here and re-run.", in_dir);
        return;
    }

    if !Path::new(out_dir).exists() {
        fs::create_dir_all(out_dir).expect("❌ Failed to create output directory");
    }

    // 2. Iterate through every file in the input directory
    let paths = fs::read_dir(in_dir).expect("❌ Failed to read input directory");
    let mut processed_count = 0;

    for path_result in paths {
        let path = path_result.expect("❌ Failed to read path").path();

        // Skip directories and focus only on files
        if path.is_file() {
            let filename = path.file_stem().unwrap().to_str().unwrap();
            let extension = path.extension().unwrap_or_default().to_str().unwrap_or_default().to_lowercase();

            // Validate image types
            if extension == "png" || extension == "jpg" || extension == "jpeg" || extension == "webp" {
                println!("\n🖼️  Processing: {}", path.display());

                let img = match image::open(&path) {
                    Ok(i) => i,
                    Err(e) => {
                        eprintln!("  ❌ Error opening {}: {}", path.display(), e);
                        continue;
                    }
                };

                let (width, height) = img.dimensions();
                let cell_width = width / 3;
                let cell_height = height / 3;

                println!("  🔪 Slicing into 3x3 grid ({}x{} per cell)...", cell_width, cell_height);

                // 3. Iterate, crop, and save
                let mut slice_index = 1;
                for row in 0..3 {
                    for col in 0..3 {
                        let x = col * cell_width;
                        let y = row * cell_height;

                        let slice = img.crop_imm(x, y, cell_width, cell_height);

                        // Output name: "original_filename_slice_1.png"
                        let out_path = format!("{}/{}_slice_{}.png", out_dir, filename, slice_index);

                        slice.save_with_format(&out_path, ImageFormat::Png)
                            .expect("  ❌ Failed to save slice");

                        println!("    ✅ Saved: {}", out_path);
                        slice_index += 1;
                    }
                }
                processed_count += 1;
            }
        }
    }

    if processed_count > 0 {
        println!("\n🚀 Batch slicing complete! {} images processed. Assets are ready in {}/", processed_count, out_dir);
    } else {
        println!("\n⚠️  No valid images found in {}/", in_dir);
    }
}
