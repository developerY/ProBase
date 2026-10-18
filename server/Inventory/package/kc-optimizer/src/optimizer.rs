use image::imageops::FilterType;
use image::{GenericImageView, ImageFormat};
use blurhash::encode;
use std::path::Path;

pub struct OptimizedAssets {
    pub blurhash: String,
    pub hero_filename: String,
    pub thumb_filename: String,
}

/// Executes the deterministic image pipeline: WebP conversion, tight cropping,
/// specific algorithmic resizing, and BlurHash generation.
pub fn process_asset_stream(raw_image_input: &str, output_dir: &Path) -> OptimizedAssets {
    let source_path = Path::new(raw_image_input);

    // Asset Naming Rule: Extract pure filename without path or extension
    // Ensures `raw_assets/KoColor/PREP/cleanser.png` becomes `cleanser.webp`
    let exact_filename = source_path.file_stem()
        .expect("Invalid image path")
        .to_str()
        .expect("Invalid UTF-8 in filename");

    let hero_filename = format!("{}.webp", exact_filename);
    let thumb_filename = format!("{}_thumb.webp", exact_filename);

    let hero_out_path = output_dir.join(&hero_filename);
    let thumb_out_path = output_dir.join(&thumb_filename);

    // 1. Load Image
    let img = image::open(source_path).unwrap_or_else(|err| {
        panic!("❌ Failed to open raw image {:?}: {}", source_path, err);
    });

    // 2. Deterministic Tight Crop (1:1 Square based on shortest dimension)
    let (width, height) = img.dimensions();
    let min_dim = width.min(height);
    let crop_x = (width - min_dim) / 2;
    let crop_y = (height - min_dim) / 2;
    let cropped_img = img.crop_imm(crop_x, crop_y, min_dim, min_dim);

    // 3. Hero Asset Stream (1024x1024)
    // Locked to Lanczos3 for highest quality detail retention
    let hero_img = cropped_img.resize_exact(1024, 1024, FilterType::Lanczos3);
    hero_img.save_with_format(&hero_out_path, ImageFormat::WebP)
        .expect("❌ Failed to save Hero WebP");

    // 4. Thumbnail Asset Stream (256x256)
    // Locked to Gaussian for efficient, soft downscaling
    let thumb_img = hero_img.resize_exact(256, 256, FilterType::Gaussian);
    thumb_img.save_with_format(&thumb_out_path, ImageFormat::WebP)
        .expect("❌ Failed to save Thumb WebP");

    // 5. BlurHash Generation
    // Extract RGBA bytes from the thumbnail to generate the 4x4 Base83 string
    let rgba_image = thumb_img.to_rgba8();
    let (th_width, th_height) = rgba_image.dimensions();

    let generated_blurhash = encode(4, 4, th_width, th_height, &rgba_image.into_raw())
        .expect("❌ Failed to generate BlurHash");

    OptimizedAssets {
        blurhash: generated_blurhash,
        hero_filename,
        thumb_filename,
    }
}
