use crate::{CielabData, SafetyFlags};
use std::f64::consts::PI;
use image::GenericImageView;

/// Maps chemistry base to thermodynamic phase.
pub fn get_chemistry_phase(base: &str) -> String {
    match base.to_uppercase().as_str() {
        "WATER" => "HYDROPHILIC_AQUEOUS".to_string(),
        "SILICONE" => "HYDROPHOBIC_SILOXANE".to_string(),
        "OIL" | "WAX" => "LIPOPHILIC_LIPID".to_string(),
        "ALCOHOL" => "VOLATILE_SOLVENT".to_string(),
        _ => "UNKNOWN_PHASE".to_string(),
    }
}

/// Scans ingredients for safety flags.
pub fn get_safety_flags(ingredients: &[String]) -> SafetyFlags {
    let mut flags = SafetyFlags {
        is_silicone_free: true,
        is_paraben_free: true,
        is_sulfate_free: true,
    };

    for ingredient in ingredients {
        let lower = ingredient.to_lowercase();

        if lower.contains("paraben") {
            flags.is_paraben_free = false;
        }

        if lower.contains("sulfate") || lower == "sls" || lower == "sles" {
            flags.is_sulfate_free = false;
        }

        if lower.ends_with("-cone") || lower.ends_with("-conol") || lower.ends_with("-siloxane")
           || lower.contains("dimethicone") || lower.contains("cyclomethicone") {
            flags.is_silicone_free = false;
        }
    }

    flags
}

/// Scans ingredients for hero actives using a dictionary.
pub fn get_hero_actives(ingredients: &[String]) -> Vec<String> {
    let heroes = vec![
        "Retinol", "Vitamin C", "Niacinamide", "Hyaluronic Acid", "Salicylic Acid",
        "Glycolic Acid", "Ceramide", "Peptide", "Squalane", "Glycerin",
        "Tocopherol", "Ferulic Acid", "Bakuchiol", "Azelaic Acid", "Panthenol"
    ];

    let mut detected = Vec::new();
    for ingredient in ingredients {
        let lower_ing = ingredient.to_lowercase();
        for hero in &heroes {
            if lower_ing.contains(&hero.to_lowercase()) {
                detected.push(hero.to_string());
            }
        }
    }
    detected.sort();
    detected.dedup();
    detected
}

/// Calculates unit price by parsing volume.
pub fn calculate_unit_price(price: Option<f64>, volume: Option<&String>) -> Option<f64> {
    let price = price?;
    let volume_str = volume?;

    // Extract first numeric match
    let numeric_part: String = volume_str.chars()
        .take_while(|c| c.is_digit(10) || *c == '.')
        .collect();

    let val: f64 = numeric_part.parse().ok()?;
    if val > 0.0 {
        Some(price / val)
    } else {
        None
    }
}

/// Generates search tokens for typo-tolerant matching.
pub fn generate_search_tokens(name: &str, brand: &str) -> Vec<String> {
    let mut tokens = Vec::new();
    let combined = format!("{} {}", brand, name).to_lowercase();

    // Split by non-alphanumeric
    for word in combined.split(|c: char| !c.is_alphanumeric()) {
        if word.len() > 1 {
            tokens.push(word.to_string());
        }
    }

    tokens.sort();
    tokens.dedup();
    tokens
}

/// Generates a BlurHash from an image URL or local path.
pub fn generate_blurhash(source: &str) -> Option<String> {
    let bytes = if source.starts_with("http") {
        reqwest::blocking::get(source).ok()?.bytes().ok()?.to_vec()
    } else {
        std::fs::read(source).ok()?
    };

    let img = image::load_from_memory(&bytes).ok()?;
    let (width, height) = img.dimensions();
    let pixels = img.to_rgba8();

    Some(blurhash::encode(4, 4, width, height, &pixels))
}

/// Converts a hex color string to CIELAB coordinates.
/// Uses D65 reference white.
pub fn hex_to_cielab(hex: &str) -> Option<CielabData> {
    let hex = hex.trim_start_matches('#');
    if hex.len() != 6 {
        return None;
    }

    let r = u8::from_str_radix(&hex[0..2], 16).ok()? as f64 / 255.0;
    let g = u8::from_str_radix(&hex[2..4], 16).ok()? as f64 / 255.0;
    let b = u8::from_str_radix(&hex[4..6], 16).ok()? as f64 / 255.0;

    // 1. sRGB to Linear RGB
    let to_linear = |c: f64| {
        if c <= 0.04045 {
            c / 12.92
        } else {
            ((c + 0.055) / 1.055).powf(2.4)
        }
    };

    let rl = to_linear(r);
    let gl = to_linear(g);
    let bl = to_linear(b);

    // 2. Linear RGB to XYZ (D65)
    let x = rl * 0.4124 + gl * 0.3576 + bl * 0.1805;
    let y = rl * 0.2126 + gl * 0.7152 + bl * 0.0722;
    let z = rl * 0.0193 + gl * 0.1192 + bl * 0.9505;

    // 3. XYZ to CIELAB (D65 Ref White)
    let xn = 0.95047;
    let yn = 1.00000;
    let zn = 1.08883;

    let f = |t: f64| {
        if t > 0.008856 {
            t.powf(1.0 / 3.0)
        } else {
            (7.787 * t) + (16.0 / 116.0)
        }
    };

    let fx = f(x / xn);
    let fy = f(y / yn);
    let fz = f(z / zn);

    let l_star = 116.0 * fy - 16.0;
    let a_star = 500.0 * (fx - fy);
    let b_star = 200.0 * (fy - fz);

    // 4. Hue Angle (hab)
    let mut h_ab = b_star.atan2(a_star) * (180.0 / PI);
    if h_ab < 0.0 {
        h_ab += 360.0;
    }

    Some(CielabData {
        l: l_star,
        a: a_star,
        b: b_star,
        hue_angle_hab: h_ab,
    })
}
