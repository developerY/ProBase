use crate::models::SafetyFlags;

/// The Enrichment Engine: Computes science and safety data at compile-time.

pub struct EnrichedData {
    pub cielab: [f32; 3],
    pub safety_flags: SafetyFlags,
    pub search_tokens: Vec<String>,
}

/// Computes CIELAB (L*a*b*) color space values from a standard Hex string (D65 Illuminant).
pub fn compute_cielab(hex_color: &str) -> [f32; 3] {
    let hex = hex_color.trim_start_matches('#');
    if hex.len() != 6 {
        return [0.0, 0.0, 0.0];
    }

    let r = u8::from_str_radix(&hex[0..2], 16).unwrap_or(0) as f64 / 255.0;
    let g = u8::from_str_radix(&hex[2..4], 16).unwrap_or(0) as f64 / 255.0;
    let b = u8::from_str_radix(&hex[4..6], 16).unwrap_or(0) as f64 / 255.0;

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

    [l_star as f32, a_star as f32, b_star as f32]
}

/// Tokenizes ingredients to flag safety markers and allergens.
pub fn compute_safety_flags(ingredients: &[String], _contains_fragrance: bool) -> SafetyFlags {
    let ingredients_lower: Vec<String> = ingredients.iter().map(|i| i.to_lowercase()).collect();

    let has_silicone = ingredients_lower.iter().any(|i| {
        i.ends_with("cone") || i.ends_with("conol") || i.ends_with("siloxane") || i.contains("dimethicone")
    });

    let has_paraben = ingredients_lower.iter().any(|i| i.contains("paraben"));

    let has_sulfate = ingredients_lower.iter().any(|i| i.contains("sulfate") || i.contains("sulphate"));

    SafetyFlags {
        is_silicone_free: !has_silicone,
        is_paraben_free: !has_paraben,
        is_sulfate_free: !has_sulfate,
    }
}

/// Generates optimized tokens for the mobile search index.
pub fn generate_search_tokens(name: &str, brand: &str, macro_cat: &str) -> Vec<String> {
    let combined = format!("{} {} {}", name, brand, macro_cat).to_lowercase();
    let mut tokens: Vec<String> = combined
        .split(|c: char| !c.is_alphanumeric())
        .filter(|s| s.len() > 1)
        .map(|s| s.to_string())
        .collect();
    tokens.sort();
    tokens.dedup();
    tokens
}

pub fn enrich_product(
    hex: &str,
    ingredients: &[String],
    fragrance: bool,
    name: &str,
    brand: &str,
    macro_cat: &str,
) -> EnrichedData {
    EnrichedData {
        cielab: compute_cielab(hex),
        safety_flags: compute_safety_flags(ingredients, fragrance),
        search_tokens: generate_search_tokens(name, brand, macro_cat),
    }
}
